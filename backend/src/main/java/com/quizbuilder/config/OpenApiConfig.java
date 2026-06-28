package com.quizbuilder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-Powered Knowledge Quiz Builder API")
                        .version("1.0.0")
                        .description("""
                                Professional REST API for the AI-Powered Knowledge Quiz Builder.
                                
                                Generate intelligent multiple-choice quizzes on any topic using AI.
                                Supports OpenAI GPT models with automatic fallback to trained local question bank.
                                
                                Features:
                                - AI-powered quiz generation (OpenAI / Local Model)
                                - Multiple difficulty levels
                                - Quiz attempt tracking
                                - Score computation with detailed feedback
                                - Leaderboard and analytics
                                - Category-based topic organization
                                """)
                        .contact(new Contact()
                                .name("Quiz Builder Team")
                                .email("support@quizbuilder.ai"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }
}
