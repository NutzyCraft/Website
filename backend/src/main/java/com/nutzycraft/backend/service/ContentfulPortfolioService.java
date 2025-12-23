package com.nutzycraft.backend.service;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAResource;
import com.nutzycraft.backend.dto.PortfolioItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContentfulPortfolioService {

    private final CDAClient client;

    public ContentfulPortfolioService(
            @Value("${contentful.space_id}") String spaceId,
            @Value("${contentful.access_token}") String accessToken) {
        this.client = CDAClient.builder()
                .setSpace(spaceId)
                .setToken(accessToken)
                .build();
    }

    public List<PortfolioItem> getPortfolioItems() {
        // Fetch entries of type 'portfolio'
        // Note: You might need to adjust the content type ID ('portfolio') 
        // if your content model ID in Contentful is different.
        List<PortfolioItem> portfolioItems = new ArrayList<>();
        
        // Fetch all entries of type "portfolio"
        // If "portfolio" is the Content Type ID.
        // If not specified, we can filter by it.
        // For simplicity, let's assume we want all entries that have the required fields or filter by contentType if known.
        // A safer bet if we don't know the exact ID but know the structure is just to fetch entries and map them. 
        // But usually content_type is required for specific queries.
        // Let's assume content type id is 'portfolio' as per instructions "entries of type portfolio".
        
        try {
            client.fetch(CDAEntry.class)
                    .withContentType("portfolio")
                    .all()
                    .items()
                    .forEach(resource -> {
                        CDAEntry entry = (CDAEntry) resource;
                        portfolioItems.add(mapToDto(entry));
                    });
        } catch (Exception e) {
            // Fallback strategy: fetch all if "portfolio" content type might be named differently or handle error
            // For now, let's stick to the instruction.
            e.printStackTrace();
        }

        return portfolioItems;
    }

    private PortfolioItem mapToDto(CDAEntry entry) {
        String title = entry.getField("heading");
        String description = entry.getField("description");
        String techStack = entry.getField("tech");
        
        // Image is likely a link to an asset
        String imageUrl = "";
        Object imageField = entry.getField("thumbnail");
        if (imageField instanceof CDAAsset) {
            imageUrl = "https:" + ((CDAAsset) imageField).url();
        }

        return new PortfolioItem(title, description, techStack, imageUrl);
    }
}
