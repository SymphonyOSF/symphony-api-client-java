package authentication;

import clients.symphony.api.APIClient;
import configuration.SymConfig;
import exceptions.AuthenticationException;
import internal.FileHelper;
import internal.jersey.JerseyHelper;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.Token;
import org.glassfish.jersey.client.ClientConfig;
import utils.HttpClientBuilderHelper;
import utils.JwtHelper;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import static internal.jersey.JerseyHelper.isNotSuccess;

@Slf4j
public class SymBotRSAAuth extends APIClient implements ISymAuth {

    private final SymConfig config;
    private final Client sessionAuthClient;
    private final Client kmAuthClient;

    private String sessionToken;
    private String kmToken;
    private String jwt;

    public SymBotRSAAuth(SymConfig config) {
        this(
            config,
            HttpClientBuilderHelper.getPodClientConfig(config),
            HttpClientBuilderHelper.getKMClientConfig(config)
        );
    }

    public SymBotRSAAuth(SymConfig config, ClientConfig sessionAuthClientConfig, ClientConfig kmAuthClientConfig) {
        this.config = config;
        final ClientBuilder clientBuilder = HttpClientBuilderHelper.getHttpClientBuilderWithTruststore(config);
        if (sessionAuthClientConfig != null) {
            this.sessionAuthClient = clientBuilder.withConfig(sessionAuthClientConfig).build();
        } else {
            this.sessionAuthClient = clientBuilder.build();
        }
        if (kmAuthClientConfig != null) {
            this.kmAuthClient = clientBuilder.withConfig(kmAuthClientConfig).build();
        } else {
            this.kmAuthClient = clientBuilder.build();
        }
    }

    @Override
    @SneakyThrows
    public void authenticate() throws AuthenticationException {

        this.refreshJwt();

        logger.debug("RSA authentication with {}", this.config.getRetry());
        final RetryConfig config = RetryConfig.custom()
            .maxAttempts(this.config.getRetry().getMaxAttempts())
            .intervalFunction(IntervalFunction.ofExponentialBackoff(
                this.config.getRetry().getInitialIntervalMillis(),
                this.config.getRetry().getMultiplier()
            ))
            .build();

        final RetryRegistry registry = RetryRegistry.of(config);

        registry.retry("Session auth").executeCheckedSupplier(() -> {
            this.sessionAuthenticate();
            return null;
        });

        registry.retry("KeyManager auth").executeCheckedSupplier(() -> {
            this.kmAuthenticate();
            return null;
        });
    }

    @Override
    public void sessionAuthenticate() throws AuthenticationException {
        logger.debug("Starting session authentication...");
        this.sessionToken = this.doRsaAuth(
            this.sessionAuthClient,
            this.config.getPodUrl(),
            AuthEndpointConstants.SESSION_AUTH_PATH_RSA,
            this.jwt
        );
        logger.debug("Session token successfully retrieved !");
    }

    @Override
    public void kmAuthenticate() throws AuthenticationException {
        logger.debug("Starting KM authentication...");
        this.kmToken = this.doRsaAuth(
            this.kmAuthClient,
            this.config.getKeyAuthUrl(),
            AuthEndpointConstants.KEY_AUTH_PATH_RSA,
            this.jwt
        );
        logger.debug("KM token successfully retrieved !");
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String getKmToken() {
        return kmToken;
    }

    @Override
    public void setKmToken(String kmToken) {
        this.kmToken = kmToken;
    }

    @Override
    public void logout() {
        logger.warn("Logout is not needed for RSA authentication.");
    }

    public String doRsaAuth(Client client, String target, String path, String jwt) throws AuthenticationException {

        final Token payload = new Token();
        payload.setToken(jwt);

        final Invocation.Builder builder = client.target(target).path(path).request(MediaType.APPLICATION_JSON);

        try (final Response response = builder.post(Entity.entity(payload, MediaType.APPLICATION_JSON))) {
            if (isNotSuccess(response)) {

                if(response.getStatus() == 401) {
                    logger.warn("JWT has expired, it will be refreshed before retry.");
                    // the JWT has expired, let's refresh it before retry
                    this.refreshJwt();
                }

                final String responsePayload = JerseyHelper.read(response, String.class);
                throw new AuthenticationException(responsePayload);
            } else {
                return JerseyHelper.read(response, Token.class).getToken();
            }
        } catch (ProcessingException ex) {
            logger.error("Unable to process RSA authentication request.", ex);
            throw new AuthenticationException(ex);
        }
    }

    private void refreshJwt() throws AuthenticationException {
        try {
            final String privateKeyPath = FileHelper.path(this.config.getBotPrivateKeyPath(), this.config.getBotPrivateKeyName());
            final PrivateKey privateKey = JwtHelper.parseRSAPrivateKey(FileHelper.readFile(privateKeyPath));
            this.jwt = JwtHelper.createSignedJwt(this.config.getBotUsername(), AuthEndpointConstants.JWT_EXPIRY_MS, privateKey);
            logger.debug("JWT successfully refresh for RSA authentication");
        } catch (IOException | GeneralSecurityException ex) {
            logger.error("Something went wrong while refreshing JWT, see cause below:", ex);
            throw new AuthenticationException(ex);
        }
    }
}
