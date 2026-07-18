package com.example.Ecomerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This build's spring-boot-starter-webmvc defaults to Jackson 3 (tools.jackson.*), but
 * springdoc-openapi still pulls classic Jackson 2 (com.fasterxml.jackson.*), and Spring MVC's
 * default converter list ends up with a plain, unconfigured Jackson 2 converter — missing
 * java.time support. Registering an ObjectMapper @Bean wasn't picked up by this build's
 * auto-configuration, so the converter is replaced directly here instead.
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        converters.add(0, new MappingJackson2HttpMessageConverter(mapper));
    }
}
