package authentication;

import static org.junit.Assert.assertEquals;
import authentication.SymOBOAuth;
import authentication.SymOBOUserRSAAuth;
import configuration.SymConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SymOBOUserRSAAuthTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void SymOBOUserRSAAuthTest() throws Exception {
    // Arrange
    SymConfig config = new SymConfig();
    JerseyClient sessionAuthClient = null;
    String username = "aaaaa";
    SymOBOAuth appAuth = null;

    // Act
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(config, sessionAuthClient, username, appAuth);

    // Assert
    assertEquals(null, symOBOUserRSAAuth.getSessionToken());
  }

  @Test
  public void SymOBOUserRSAAuthTest2() throws Exception {
    // Arrange
    SymConfig config = new SymConfig();
    JerseyClient sessionAuthClient = null;
    Long uid = new Long(1L);
    SymOBOAuth appAuth = null;

    // Act
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(config, sessionAuthClient, uid, appAuth);

    // Assert
    assertEquals(null, symOBOUserRSAAuth.getSessionToken());
  }

  @Test
  public void authenticateTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act and Assert
    thrown.expect(NullPointerException.class);
    symOBOUserRSAAuth.authenticate();
  }

  @Test
  public void getKmTokenTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act and Assert
    thrown.expect(RuntimeException.class);
    symOBOUserRSAAuth.getKmToken();
  }

  @Test
  public void getSessionTokenTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act
    String actual = symOBOUserRSAAuth.getSessionToken();

    // Assert
    assertEquals(null, actual);
  }

  @Test
  public void kmAuthenticateTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act and Assert
    thrown.expect(RuntimeException.class);
    symOBOUserRSAAuth.kmAuthenticate();
  }

  @Test
  public void logoutTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act
    symOBOUserRSAAuth.logout();

    // Assert
    assertEquals(null, symOBOUserRSAAuth.getSessionToken());
  }

  @Test
  public void sessionAuthenticateTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);

    // Act and Assert
    thrown.expect(NullPointerException.class);
    symOBOUserRSAAuth.sessionAuthenticate();
  }

  @Test
  public void setKmTokenTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);
    String kmToken = "aaaak";

    // Act and Assert
    thrown.expect(RuntimeException.class);
    symOBOUserRSAAuth.setKmToken(kmToken);
  }

  @Test
  public void setSessionTokenTest() throws Exception {
    // Arrange
    SymConfig symConfig = new SymConfig();
    SymOBOUserRSAAuth symOBOUserRSAAuth = new SymOBOUserRSAAuth(symConfig, null, new Long(1L), null);
    String sessionToken = "aaaak";

    // Act
    symOBOUserRSAAuth.setSessionToken(sessionToken);

    // Assert
    assertEquals("aaaak", symOBOUserRSAAuth.getSessionToken());
  }
}
