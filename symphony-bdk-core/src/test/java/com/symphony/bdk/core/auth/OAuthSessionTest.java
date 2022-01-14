package com.symphony.bdk.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.http.api.ApiException;

import org.junit.jupiter.api.Test;

class OAuthSessionTest {
  private final AuthSession authSession = mock(AuthSession.class);

  @Test
  void testGetBearerToken() throws AuthUnauthorizedException, ApiException {
    String token = "Bearer Token";
    when(authSession.getAuthorizationToken()).thenReturn(token);

    OAuthSession session = new OAuthSession(authSession);
    assertEquals(token, session.getBearerToken());
  }

  @Test
  void testGetBearerTokenFails() throws AuthUnauthorizedException {
    when(authSession.getAuthorizationToken()).thenThrow(AuthUnauthorizedException.class);

    OAuthSession session = new OAuthSession(authSession);
    assertThrows(ApiException.class, session::getBearerToken);
  }

}
