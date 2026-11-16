package io.skillia.restcontroller.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocOpenApiConf {
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            // Define some reusable responses that can be referenced for common error scenarios
            .components(new Components()
                .addResponses("BadRequestProblem",
                    new ApiResponse()
                        .description("Bad Request")
                        .content(problemContent())
                )
                .addResponses("NotFoundProblem",
                    new ApiResponse()
                        .description("Not Found")
                        .content(problemContent())
                )
                .addResponses("ConflictProblem",
                    new ApiResponse()
                        .description("Conflict")
                        .content(problemContent())
                )
            );
    }

    private static Content problemContent() {
        return new Content()
            .addMediaType(
                "application/problem+json",
                new MediaType()
                    .schema(
                        new Schema<>().$ref("#/components/schemas/ProblemDetail")
                    )
            );
    }
}
