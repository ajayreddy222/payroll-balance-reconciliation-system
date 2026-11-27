package com.payroll.reconciliation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Registers a Bearer token security scheme so the Swagger UI
 * shows the Authorize button — allowing users to paste their JWT
 * and test protected endpoints directly from the browser.</p>
 *
 * <p>Swagger UI is available at:
 * <a href="http://localhost:8080/swagger-ui/index.html">
 * http://localhost:8080/swagger-ui/index.html</a></p>
 *
 * @author Payroll Reconciliation Team
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Configures the OpenAPI specification with application info and
     * a global Bearer JWT security requirement.
     *
     * @return the customized OpenAPI bean
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payroll Reconciliation API")
                        .description("REST API for the Payroll Balance Reconciliation System")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
