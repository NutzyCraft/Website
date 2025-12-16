package com.nutzycraft.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> verifyToken(String idToken) {
        try {
            // Validate token against Google's server
            // Returns a Map with keys like "email", "name", "aud", etc.
            ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {};
            
            // Create an empty HttpEntity to avoid passing null for the request body,
            // which can sometimes trigger nullability warnings in strict analysis.
            // HttpMethod.GET is an enum constant and is inherently non-null.
            HttpEntity<String> requestEntity = new HttpEntity<>(new HttpHeaders());

            // Suppress null warning for strict analysis if it incorrectly flags HttpMethod.GET or the requestEntity.
            // HttpMethod.GET is not null, and requestEntity is now explicitly created.
            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GOOGLE_TOKEN_INFO_URL + idToken, 
                HttpMethod.GET, 
                requestEntity, 
                typeRef
            );
            
            Map<String, Object> payload = response.getBody();

            if (payload == null) {
                throw new RuntimeException("Empty response from Google");
            }

            String aud = (String) payload.get("aud");
            if (!"395279487546-l48b8apos5dpa0fctvltp6hhsfl10o83.apps.googleusercontent.com".equals(aud)) {
                throw new RuntimeException("Invalid Client ID in Token");
            }

            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Google Token");
        }
    }
}
