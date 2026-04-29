package io.camunda.example.classic.integration;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

/**
 * Integration test for PDF Merge & Split Connector.
 * Tests the connector in a full Camunda runtime environment.
 * 
 * NOTE: This test requires Docker to be running and will pull camunda/camunda:8.9.1 image.
 * Disabled by default for faster builds. Enable when Docker is available.
 */
@Disabled("Requires Docker - enable manually when needed")
@SpringBootTest(
    classes = {TestConnectorRuntimeApplication.class},
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamundaSpringProcessTest
public class PdfConnectorIntegrationTest {

  @Autowired
  private CamundaClient client;

  @Test
  void testPdfMergeOperation() {
    // given - create a process instance with PDF merge operation
    final var processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId("operation-connector-test-process")
            .latestVersion()
            .send()
            .join();

    // then - verify the process completes successfully
    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byName("MyConnector"))
        .isCompleted();
  }
}
