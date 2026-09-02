package com.seopulse.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SEOPulse API",
                version = "v1",
                description = "Rest API For the SEOPulse",
                contact = @Contact(
                        name = "SEOPulse"
                )
        )
)
public class OpenApiConfig {
}
