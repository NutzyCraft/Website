package com.nutzycraft.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from the parent directory (d:\Website\)
        registry.addResourceHandler("/**")
                .addResourceLocations("file:../")
                .setCachePeriod(0); // Disable caching for development
    }
}
