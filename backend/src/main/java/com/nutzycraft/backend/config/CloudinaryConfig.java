package com.nutzycraft.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "defxqfmyu",
            "api_key", "272942611646733",
            "api_secret", "iRt-B-wwElu7U0OG0Eq-5TdIWAc"
        ));
    }
}
