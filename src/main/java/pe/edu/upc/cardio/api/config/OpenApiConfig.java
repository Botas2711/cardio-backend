package pe.edu.upc.cardio.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("CardioPredict API - Sistema de Predicción y Gestión del Riesgo Cardiovascular")
                        .description("API RESTful para la adquisición telemétrica PPG, soporte clínico a la decisión (CDSS), alertas de auxilio familiar y minería de procesos (PM4Py). Hospital II Carlos Alcántara Butterfield.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Investigación UPC")
                                .email("u202012345@upc.edu.pe"))
                        .license(new License().name("Ley N.° 29733 de Protección de Datos Personales en el Perú")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
