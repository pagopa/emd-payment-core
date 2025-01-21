package it.gov.pagopa.template.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class SwaggerConfig.
 */
@Configuration
public class SwaggerConfig {

  /** The title. */
  private final String title;

  /** The description. */
  private final String description;

  /** The version. */
  private final String version;

  public SwaggerConfig(
    @Value("${swagger.title:${spring.application.name}}") String title,
    @Value("${swagger.description:Api and Models}") String description,
    @Value("${swagger.version:${spring.application.version}}") String version) {
    this.title = title;
    this.description = description;
    this.version = version;
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().components(new Components()).info(new Info()
        .title(title)
        .description(description)
        .version(version));
  }
}
