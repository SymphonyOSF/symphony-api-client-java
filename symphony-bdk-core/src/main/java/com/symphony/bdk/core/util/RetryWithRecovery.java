package com.symphony.bdk.core.util;

import com.symphony.bdk.core.api.invoker.ApiException;
import com.symphony.bdk.core.config.model.BdkRetryConfig;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class RetryWithRecovery<T> {
  private SupplierWithApiException<T> supplier;
  private Map<Predicate<ApiException>, ConsumerWithThrowable> recoveryStrategies;
  private Retry retry;

  public RetryWithRecovery(String name, SupplierWithApiException<T> supplier,
      Predicate<Throwable> retryOnExceptionPredicate,
      BdkRetryConfig bdkRetryConfig, Map<Predicate<ApiException>, ConsumerWithThrowable> recoveryStrategies) {
    this.supplier = supplier;
    this.recoveryStrategies = recoveryStrategies;
    this.retry = createRetry(name, bdkRetryConfig, retryOnExceptionPredicate);
  }

  public T execute() throws Throwable {
    return this.retry.executeCheckedSupplier(this::executeMainAndRecoveryStrategies);
  }

  private T executeMainAndRecoveryStrategies() throws Throwable {
    try {
      return supplier.get();
    } catch (ApiException e) {
      handleRecovery(e);
      throw e;
    }
  }

  private void handleRecovery(ApiException e) throws Throwable {
    boolean recoveryTriggered = false;

    for (Map.Entry<Predicate<ApiException>, ConsumerWithThrowable> entry : recoveryStrategies.entrySet()) {
      if (entry.getKey().test(e)) {
        recoveryTriggered = true;
        entry.getValue().consume(e);
      }
    }

    if (!recoveryTriggered) {
      log.error("Error {}: {}", e.getCode(), e.getMessage());
    }
  }

  private Retry createRetry(String name, BdkRetryConfig bdkRetryConfig,
      Predicate<Throwable> retryOnExceptionPredicate) {
    final RetryConfig retryConfig = RetryConfig.custom()
        .maxAttempts(bdkRetryConfig.getMaxAttempts())
        .intervalFunction(BdkExponentialFunction.ofExponentialBackoff(bdkRetryConfig))
        .retryOnException(retryOnExceptionPredicate)
        .build();

    Retry retry = Retry.of(name, retryConfig);
    retry.getEventPublisher().onRetry(event -> {
      long intervalInMillis = event.getWaitInterval().toMillis();
      double interval = intervalInMillis / 1000.0;
      if (event.getLastThrowable() != null) {
        log.debug("{} service failed due to {}", name, event.getLastThrowable().getMessage());
      }
      log.info("Retry in {} secs...", interval);
    });

    return retry;
  }
}