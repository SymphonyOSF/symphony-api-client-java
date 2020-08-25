package com.symphony.bdk.core.auth.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;

import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Test class for {@link AuthSessionCertImpl}
 */
public class AuthSessionCertImplTest {

  @Test
  void testRefresh() throws AuthUnauthorizedException {

    final String sessionToken = UUID.randomUUID().toString();
    final String kmToken = UUID.randomUUID().toString();

    final BotAuthenticatorCertImpl auth = mock(BotAuthenticatorCertImpl.class);
    when(auth.retrieveSessionToken()).thenReturn(sessionToken);
    when(auth.retrieveKeyManagerToken()).thenReturn(kmToken);

    final AuthSessionCertImpl session = new AuthSessionCertImpl(auth);
    session.refresh();

    assertEquals(sessionToken, session.getSessionToken());
    assertEquals(kmToken, session.getKeyManagerToken());

    verify(auth, times(1)).retrieveSessionToken();
    verify(auth, times(1)).retrieveKeyManagerToken();
  }
}
