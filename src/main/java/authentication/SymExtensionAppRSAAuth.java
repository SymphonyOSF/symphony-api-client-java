package authentication;

import authentication.extensionapp.InMemoryTokensRepository;
import authentication.extensionapp.TokensRepository;
import clients.symphony.api.APIClient;
import clients.symphony.api.constants.CommonConstants;
import configuration.SymConfig;
import exceptions.NoConfigException;
import model.AppAuthResponse;
import model.PodCert;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpClientBuilderHelper;
import utils.JwtHelper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static utils.JwtHelper.validateJwt;

public final class SymExtensionAppRSAAuth extends APIClient {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Logger logger = LoggerFactory.getLogger(SymExtensionAppRSAAuth.class);

    private SymConfig config;
    private Client sessionAuthClient;
    private String jwt;
    private int authRetries = 0;
    private TokensRepository tokensRepository;
    private String podCertificate;
    private PrivateKey appPrivateKey;

    /**
     * Create an instance initialized with provided configuration.
     *
     * @param config the Symphony configuration
     */
    public SymExtensionAppRSAAuth(final SymConfig config) {
        this(config, null);
    }

    /**
     * Create an instance initialized with provided configuration and app RSA private key. The parts of the configuration
     * related to app RSA private key will be ignored, e.g. {@link SymConfig#getAppPrivateKeyPath()} and
     * {@link SymConfig#getAppPrivateKeyName()}. If given private key is null, then the initialization will only use the
     * configuration, see {@link SymExtensionAppRSAAuth#SymExtensionAppRSAAuth(SymConfig)}.
     *
     * @param config        the Symphony configuration
     * @param appPrivateKey the RSA private key
     */
    public SymExtensionAppRSAAuth(final SymConfig config, PrivateKey appPrivateKey) {
        this.config = config;
        this.appPrivateKey = appPrivateKey;

        ClientBuilder clientBuilder = HttpClientBuilderHelper.getHttpClientBuilderWithTruststore(this.config);
        Client client = clientBuilder.build();

        if (isEmpty(this.config.getProxyURL()) && isEmpty(this.config.getPodProxyURL())) {
            this.sessionAuthClient = client;
        } else {
            this.sessionAuthClient = clientBuilder
                    .withConfig(HttpClientBuilderHelper.getClientConfig(this.config))
                    .build();
        }
        this.tokensRepository = new InMemoryTokensRepository();

        setupPodCertificate();
    }

    public SymExtensionAppRSAAuth(SymConfig config, ClientConfig sessionAuthClientConfig,
        TokensRepository tokensRepository) {
        this.config = config;
        ClientBuilder clientBuilder =
            HttpClientBuilderHelper.getHttpClientBuilderWithTruststore(config);
        if (sessionAuthClientConfig != null) {
            this.sessionAuthClient = clientBuilder.withConfig(sessionAuthClientConfig).build();
        } else {
            this.sessionAuthClient = clientBuilder.build();
        }
        this.tokensRepository = tokensRepository;
        setupPodCertificate();
    }

    public AppAuthResponse appAuthenticate() {
        PrivateKey appPrivateKey = getAppPrivateKey();
        if (config != null) {
            logger.info("RSA extension app auth");
            jwt = JwtHelper.createSignedJwt(config.getAppId(), AuthEndpointConstants.JWT_EXPIRY_MS, appPrivateKey);
            Map<String, String> token = new HashMap<>();
            token.put("appToken", generateToken());
            token.put("authToken", jwt);
            Response response
                = sessionAuthClient.target(CommonConstants.HTTPS_PREFIX +
                config.getSessionAuthHost() + ":" + config.getSessionAuthPort())
                .path(AuthEndpointConstants.SESSION_EXT_APP_AUTH_PATH_RSA)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(token, MediaType.APPLICATION_JSON));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                try {
                    handleError(response, null);
                } catch (Exception e) {
                    logger.error("Unexpected error, retry authentication in 30 seconds");
                }
                try {
                    TimeUnit.SECONDS.sleep(AuthEndpointConstants.TIMEOUT);
                } catch (InterruptedException e) {
                    logger.error("Error with authentication", e);
                }
                if (authRetries++ > AuthEndpointConstants.MAX_AUTH_RETRY) {
                    logger.error("Max retries reached. Giving up on auth.");
                    return null;
                }
                appAuthenticate();
            } else {
                AppAuthResponse appAuthResponse = response.readEntity(AppAuthResponse.class);
                return tokensRepository.save(appAuthResponse);
            }
        } else {
            throw new NoConfigException("Must provide a SymConfig object to authenticate");

        }
        return null;
    }

    private PrivateKey getAppPrivateKey() {
        if (appPrivateKey == null) {
            try {
                String privateKeyFilePath = config.getAppPrivateKeyPath() + config.getAppPrivateKeyName();
                appPrivateKey = JwtHelper.parseRSAPrivateKey(new File(privateKeyFilePath));
            } catch (IOException | GeneralSecurityException e) {
                logger.error("Failed to obtain app RSA private key. An exception occurred parsing app RSA file", e);
            }
        }

        return appPrivateKey;
    }

    private String generateToken() {
        byte[] randBytes = new byte[64];
        secureRandom.nextBytes(randBytes);
        return Hex.encodeHexString(randBytes);
    }

    public Boolean validateTokens(String appToken, String symphonyToken) {
        return tokensRepository.get(appToken)
            .filter(token -> token.getSymphonyToken().equals(symphonyToken))
            .isPresent();
    }

    public Object verifyJWT(final String jwt) {
        return validateJwt(jwt, podCertificate);
    }

    public PublicKey getPodPublicKey() throws CertificateException {
        String encoded = podCertificate.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
        byte[] decoded = Base64.decodeBase64(encoded);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate
          x509Certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(decoded));
        return x509Certificate.getPublicKey();
    }

    private void setupPodCertificate(){
      String authUrl = config.getSessionAuthHost() + ":" + config.getSessionAuthPort();
      Response response
        = sessionAuthClient.target(CommonConstants.HTTPS_PREFIX + authUrl)
        .path(AuthEndpointConstants.POD_CERT_RSA_PATH)
        .request(MediaType.APPLICATION_JSON)
        .get();
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        try {
          handleError(response, null);
        } catch (Exception e) {
          logger.error("Unexpected error, retry authentication in 30 seconds");
        }
        try {
          TimeUnit.SECONDS.sleep(AuthEndpointConstants.TIMEOUT);
        } catch (InterruptedException e) {
          logger.error("Error with verify", e);
        }
      } else {
        podCertificate = response.readEntity(PodCert.class).getCertificate();
      }
    }
}
