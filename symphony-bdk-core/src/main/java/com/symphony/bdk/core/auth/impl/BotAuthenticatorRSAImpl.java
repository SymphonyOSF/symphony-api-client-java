package com.symphony.bdk.core.auth.impl;

import com.symphony.bdk.core.api.invoker.ApiClient;
import com.symphony.bdk.core.api.invoker.ApiException;
import com.symphony.bdk.core.api.invoker.ApiRuntimeException;
import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.core.auth.BotAuthenticator;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.auth.jwt.JwtHelper;
import com.symphony.bdk.gen.api.AuthenticationApi;
import com.symphony.bdk.gen.api.model.AuthenticateRequest;
import com.symphony.bdk.gen.api.model.Token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;

import java.security.PrivateKey;

import javax.annotation.Nonnull;

/**
 * Bot authenticator RSA implementation.
 *
 * @see <a href="https://developers.symphony.com/symphony-developer/docs/rsa-bot-authentication-workflow">RSA Bot Authentication Workflow</a>
 */
@Slf4j
@RequiredArgsConstructor
@API(status = API.Status.INTERNAL)
public class BotAuthenticatorRSAImpl implements BotAuthenticator {

  private final String username;
  private final PrivateKey privateKey;

  private final ApiClient loginApiClient;
  private final ApiClient relayApiClient;

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nonnull AuthSession authenticateBot() {
    return new AuthSessionImpl(this);
  }

  public String retrieveSessionToken() throws AuthUnauthorizedException {
    log.debug("Start retrieving sessionToken using RSA authentication...");
    return this.doRetrieveToken(this.loginApiClient);
  }

  public String retrieveKeyManagerToken() throws AuthUnauthorizedException {
    log.debug("Start retrieving keyManagerToken using RSA authentication...");
    return this.doRetrieveToken(this.relayApiClient);
  }

  protected String doRetrieveToken(ApiClient client) throws AuthUnauthorizedException {
    final String jwt = JwtHelper.createSignedJwt(this.username, 30_000, this.privateKey);
    final AuthenticateRequest req = new AuthenticateRequest();
    req.setToken(jwt);

    try {
      final Token token = new AuthenticationApi(client).pubkeyAuthenticatePost(req);
      log.debug("{} successfully retrieved.", token.getName());
      return token.getToken();
    } catch (ApiException ex) {
      if (ex.getCode() == 401) {
        // usually happens when the public RSA is wrong or if the username is not correct
        throw new AuthUnauthorizedException("Service account with username '" + this.username + "' is not authorized to authenticate. Check if the public RSA key is valid or if the username is correct.", ex);
      } else {
        // we don't know what to do, let's forward the ApiException
        throw new ApiRuntimeException(ex);
      }
    }
  }
}
