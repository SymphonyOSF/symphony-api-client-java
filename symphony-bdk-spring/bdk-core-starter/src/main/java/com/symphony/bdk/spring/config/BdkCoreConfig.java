package com.symphony.bdk.spring.config;

import com.symphony.bdk.core.api.invoker.jersey2.ApiClientBuilderProviderJersey2;
import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.auth.AuthenticatorFactory;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.client.ApiClientFactory;
import com.symphony.bdk.spring.SymphonyBdkCoreProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuration and injection of the main BDK/Core classes as beans within the Spring application context.
 */
@Slf4j
public class BdkCoreConfig {

  @Primary
  @Bean(name = "defaultSymphonyBdkApiClientFactory")
  public ApiClientFactory apiClientFactory(SymphonyBdkCoreProperties properties) {
    return new ApiClientFactory(properties, new ApiClientBuilderProviderJersey2()); // TODO create RestTemplate/or WebClient implementation
  }

  @Primary
  @Bean(name = "defaultSymphonyBdkAuthenticatorFactory")
  public AuthenticatorFactory authenticatorFactory(SymphonyBdkCoreProperties properties, ApiClientFactory apiClientFactory) {
    return new AuthenticatorFactory(properties, apiClientFactory);
  }

  @Primary
  @Bean(name = "defaultSymphonyBdkBotSession")
  public AuthSession botSession(AuthenticatorFactory authenticatorFactory) {
    try {
      return authenticatorFactory.getBotAuthenticator().authenticateBot();
    } catch (AuthUnauthorizedException | AuthInitializationException e) {
      throw new BeanInitializationException("Unable to authenticate bot", e);
    }
  }
}
