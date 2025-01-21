package it.gov.pagopa.template.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SwaggerConfigTest {

  private final Info expectedInfo = new Info()
    .title("TITLE")
    .description("DESCRPTION")
    .version("VERSION");

  private final SwaggerConfig swaggerConfig = new SwaggerConfig(expectedInfo.getTitle(), expectedInfo.getDescription(), expectedInfo.getVersion());

  @Test
  void whenCustomOpenAPIThenExpectedInfo(){
    // When
    OpenAPI result = swaggerConfig.customOpenAPI();

    // Then
    Assertions.assertEquals(expectedInfo, result.getInfo());
  }
}
