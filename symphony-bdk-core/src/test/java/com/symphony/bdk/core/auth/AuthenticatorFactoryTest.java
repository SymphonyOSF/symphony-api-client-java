package com.symphony.bdk.core.auth;

import com.symphony.bdk.core.api.invoker.ApiClient;
import com.symphony.bdk.core.api.invoker.jersey2.ApiClientBuilderJersey2;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.impl.BotAuthenticatorCertImpl;
import com.symphony.bdk.core.auth.impl.BotAuthenticatorRsaImpl;
import com.symphony.bdk.core.auth.impl.OboAuthenticatorRsaImpl;
import com.symphony.bdk.core.client.ApiClientFactory;
import com.symphony.bdk.core.config.model.BdkConfig;
import com.symphony.bdk.core.test.RsaTestHelper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the {@link AuthenticatorFactory}.
 */
class AuthenticatorFactoryTest {

  private static final ApiClient DUMMY_API_CLIENT = new ApiClientBuilderJersey2().build();
  private static final String RSA_PRIVATE_KEY = RsaTestHelper.generatePrivateKeyAsString();
  private static ApiClientFactory DUMMY_API_CLIENT_FACTORY;

  @SneakyThrows
  @BeforeAll
  public static void setup() {
    DUMMY_API_CLIENT_FACTORY = mock(ApiClientFactory.class);
    when(DUMMY_API_CLIENT_FACTORY.getLoginClient()).thenReturn(DUMMY_API_CLIENT);
    when(DUMMY_API_CLIENT_FACTORY.getRelayClient()).thenReturn(DUMMY_API_CLIENT);
    when(DUMMY_API_CLIENT_FACTORY.getSessionAuthClient()).thenReturn(DUMMY_API_CLIENT);
    when(DUMMY_API_CLIENT_FACTORY.getKeyAuthClient()).thenReturn(DUMMY_API_CLIENT);
  }

  @Test
  void testGetBotAuthenticatorWithValidPrivateKey(@TempDir Path tempDir) throws AuthInitializationException {

    final BdkConfig config = createRsaConfig(() -> {
      final Path privateKeyPath = tempDir.resolve(UUID.randomUUID().toString() + "-privateKey.pem");
      writeContentToPath(RSA_PRIVATE_KEY, privateKeyPath);
      return privateKeyPath.toAbsolutePath().toString();
    });

    final AuthenticatorFactory factory = new AuthenticatorFactory(config, DUMMY_API_CLIENT_FACTORY);
    final BotAuthenticator botAuth = factory.getBotAuthenticator();
    assertNotNull(botAuth);
    assertEquals(BotAuthenticatorRsaImpl.class, botAuth.getClass());
  }

  @Test
  void testGetBotAuthenticatorWithInvalidPrivateKey(@TempDir Path tempDir) {

    final BdkConfig config = createRsaConfig(() -> {
      final Path privateKeyPath = tempDir.resolve(UUID.randomUUID().toString() + "-privateKey.pem");
      writeContentToPath("invalid-private-key-content", privateKeyPath);
      return privateKeyPath.toAbsolutePath().toString();
    });

    final AuthenticatorFactory factory = new AuthenticatorFactory(config, DUMMY_API_CLIENT_FACTORY);
    assertThrows(AuthInitializationException.class, factory::getBotAuthenticator);
  }

  @Test
  void testGetBotAuthenticatorWithNotFoundPrivateKey(@TempDir Path tempDir) {

    final BdkConfig config = createRsaConfig(() -> tempDir.resolve(UUID.randomUUID().toString() + "-privateKey.pem").toAbsolutePath().toString());

    final AuthenticatorFactory factory = new AuthenticatorFactory(config, DUMMY_API_CLIENT_FACTORY);
    assertThrows(AuthInitializationException.class, factory::getBotAuthenticator);
  }

  @Test
  void testGetOboAuthenticatorWithValidPrivateKey(@TempDir Path tempDir) throws AuthInitializationException {

    final BdkConfig config = createRsaConfig(() -> {
      final Path privateKeyPath = tempDir.resolve(UUID.randomUUID().toString() + "-privateKey.pem");
      writeContentToPath(RSA_PRIVATE_KEY, privateKeyPath);
      return privateKeyPath.toAbsolutePath().toString();
    });

    final AuthenticatorFactory factory = new AuthenticatorFactory(config, DUMMY_API_CLIENT_FACTORY);
    final OboAuthenticator oboAuth = factory.getOboAuthenticator();
    assertNotNull(oboAuth);
    assertEquals(OboAuthenticatorRsaImpl.class, oboAuth.getClass());
  }

  @Test
  void testGetBotCertificateAuthenticator() throws AuthInitializationException {
    final BdkConfig config = new BdkConfig();
    config.getBot().setCertificatePath("/path/to/cert/file.p12");
    config.getBot().setCertificatePassword("password");

    final AuthenticatorFactory factory = new AuthenticatorFactory(config, DUMMY_API_CLIENT_FACTORY);
    BotAuthenticator botAuthenticator = factory.getBotAuthenticator();
    assertNotNull(botAuthenticator);
    assertEquals(BotAuthenticatorCertImpl.class, botAuthenticator.getClass());
  }

  @SneakyThrows
  private BdkConfig createRsaConfig(Supplier<String> privateKeyPathSupplier) {
    final BdkConfig config = new BdkConfig();

    config.getBot().setPrivateKeyPath(privateKeyPathSupplier.get());
    config.getBot().setUsername("bot-" + UUID.randomUUID().toString());

    config.getApp().setPrivateKeyPath(privateKeyPathSupplier.get());
    config.getApp().setAppId("app-" + UUID.randomUUID().toString());

    return config;
  }

  @SneakyThrows
  private static void writeContentToPath(String content, Path path) {
    IOUtils.write(content, new FileOutputStream(path.toFile()), "utf-8");
  }
}
