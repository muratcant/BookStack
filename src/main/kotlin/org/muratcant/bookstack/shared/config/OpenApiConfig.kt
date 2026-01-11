package org.muratcant.bookstack.shared.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("BookStack API")
                .version("1.0.0")
                .description("Hybrid bookstore and reading library REST API")
        )
        .tags(
            listOf(
                Tag().name("Member").description("Member management operations"),
                Tag().name("Book").description("Book catalog operations"),
                Tag().name("Copy").description("Physical book copy operations"),
                Tag().name("Visit").description("Check-in/Check-out operations"),
                Tag().name("Loan").description("Borrowing and returning operations"),
                Tag().name("Reservation").description("Book reservation operations"),
                Tag().name("Penalty").description("Penalty management operations")
            )
        )
}

