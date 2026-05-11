package com.example.sistemanutricao.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceCacheConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/output.css")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/Scripts/**")
            .addResourceLocations("classpath:/static/Scripts/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/imagens/**")
            .addResourceLocations("classpath:/static/imagens/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());

        registry.addResourceHandler("/imagens-perfil/**")
            .addResourceLocations("file:./uploads/imagens-perfil/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());
    }
}
