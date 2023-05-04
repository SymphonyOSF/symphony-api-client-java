package com.symphony.bdk.app.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.symphony.bdk.core.service.health.HealthService;
import com.symphony.bdk.gen.api.model.V3Health;
import com.symphony.bdk.gen.api.model.V3HealthComponent;
import com.symphony.bdk.gen.api.model.V3HealthStatus;
import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.ApiRuntimeException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class SymphonyBdkHealthIndicatorTest {
  @Mock HealthService healthService;
  @InjectMocks SymphonyBdkHealthIndicator healthIndicator;

  @Test
  void doHealthCheck_successful() throws Exception {
    V3Health health = new V3Health();
    health.putServicesItem("pod", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putServicesItem("datafeed", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putServicesItem("key_manager", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putUsersItem("agentservice", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putUsersItem("ceservice", new V3HealthComponent().status(V3HealthStatus.UP));
    when(healthService.healthCheckExtended()).thenReturn(health);
    when(healthService.datafeedHealthCheck()).thenReturn(V3HealthStatus.UP);
    Health.Builder builder = new Health.Builder();
    healthIndicator.doHealthCheck(builder);
    Health build = builder.build();
    assertThat(build.getStatus().getCode()).isEqualTo("UP");
  }

  @Test
  void doHealthCheck_withCEDown_up() throws Exception {
    V3Health health = new V3Health();
    health.putServicesItem("pod", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putServicesItem("datafeed", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putServicesItem("key_manager", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putUsersItem("agentservice", new V3HealthComponent().status(V3HealthStatus.UP));
    health.putUsersItem("ceservice", new V3HealthComponent().status(V3HealthStatus.DOWN));
    when(healthService.healthCheckExtended()).thenReturn(health);
    when(healthService.datafeedHealthCheck()).thenReturn(V3HealthStatus.UP);
    Health.Builder builder = new Health.Builder();
    healthIndicator.doHealthCheck(builder);
    Health build = builder.build();
    assertThat(build.getStatus().getCode()).isEqualTo("UP");
  }

  @Test
  void doHealthCheck_exception() throws Exception {
    final String body = "{\n"
        + "  \"status\": \"UP\",\n"
        + "  \"version\": \"2.57.0\",\n"
        + "  \"services\": {\n"
        + "    \"pod\": {\n"
        + "      \"status\": \"UP\",\n"
        + "      \"version\": \"1.57.0\"\n"
        + "    },\n"
        + "    \"datafeed\": {\n"
        + "      \"status\": \"UP\",\n"
        + "      \"version\": \"2.1.28\"\n"
        + "    },\n"
        + "    \"key_manager\": {\n"
        + "      \"status\": \"UP\",\n"
        + "      \"version\": \"1.56.0\"\n"
        + "    }\n"
        + "  },\n"
        + "  \"users\": {\n"
        + "    \"agentservice\": {\n"
        + "      \"status\": \"DOWN\"\n"
        + "    },\n"
        + "    \"ceservice\": {\n"
        + "      \"status\": \"DOWN\",\n"
        + "      \"message\": \"Ceservice authentication credentials missing or misconfigured\"\n"
        + "    }\n"
        + "  }\n"
        + "}";


    doThrow(new ApiRuntimeException(new ApiException(503, "message", Collections.EMPTY_MAP, body))).when(healthService)
        .healthCheckExtended();
    when(healthService.datafeedHealthCheck()).thenReturn(V3HealthStatus.UP);
    Health.Builder builder = new Health.Builder();
    healthIndicator.doHealthCheck(builder);
    Health build = builder.build();
    assertThat(build.getStatus().getCode()).isEqualTo("WARNING");
  }
}