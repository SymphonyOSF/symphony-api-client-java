package configuration;

import clients.symphony.api.constants.CommonConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SymConfig {

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10_000;
    private static final int DEFAULT_READ_TIMEOUT = 35_000;

    // ---------------------------------------------------------------------------------------------------------------//
    // NETWORK
    //
    private String sessionAuthHost;
    private int sessionAuthPort;
    private String sessionAuthContextPath;

    private String keyAuthHost;
    private int keyAuthPort;
    private String keyAuthContextPath;
    private String keyManagerProxyURL;
    private String keyManagerProxyUsername;
    private String keyManagerProxyPassword;

    private String podHost;
    private int podPort;
    private String podContextPath;
    private String podProxyURL;
    private String podProxyUsername;
    private String podProxyPassword;

    private String agentHost;
    private int agentPort;
    private String agentContextPath;
    private String agentProxyURL;
    private String agentProxyUsername;
    private String agentProxyPassword;

    private String proxyURL;
    private String proxyUsername;
    private String proxyPassword;



    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    // ---------------------------------------------------------------------------------------------------------------//
    // AUTHENTICATION
    //
    private String botUsername;
    private String botEmailAddress;
    // rsa
    private String botPrivateKeyPath;
    private String botPrivateKeyName;
    // cert
    private String botCertPath;
    private String botCertName;
    private String botCertPassword;

    private String appId;
    // rsa
    private String appPrivateKeyPath;
    private String appPrivateKeyName;
    // cert
    private String appCertPath;
    private String appCertName;
    private String appCertPassword;

    // ---------------------------------------------------------------------------------------------------------------//
    // SSL
    //
    private String truststorePath;
    private String truststorePassword;

    // ---------------------------------------------------------------------------------------------------------------//
    // DATAFEED
    private String datafeedVersion;
    private int datafeedEventsThreadpoolSize;
    private int datafeedEventsErrorTimeout;
    private Boolean reuseDatafeedID;
    private String datafeedIdFilePath;

    // ---------------------------------------------------------------------------------------------------------------//
    // MISC
    //
    private String authenticationFilterUrlPattern;
    private boolean showFirehoseErrors;
    private ArrayList<String> supportedUriSchemes = new ArrayList<>();
    private RetryConfiguration retry = new RetryConfiguration();

    public String getAgentUrl() {
        String port = (this.getAgentPort() == 443) ? "" : ":" + this.getAgentPort();
        String contextPath = formatContextPath(this.getAgentContextPath());
        return CommonConstants.HTTPS_PREFIX + this.getAgentHost() + port + contextPath;
    }

    public String getPodUrl() {
        String port = (this.getPodPort() == 443) ? "" : ":" + this.getPodPort();
        String contextPath = formatContextPath(this.getPodContextPath());
        return CommonConstants.HTTPS_PREFIX + this.getPodHost() + port + contextPath;
    }

    public String getKeyAuthUrl() {
        String port = (this.getKeyAuthPort() == 443) ? "" : ":" + this.getKeyAuthPort();
        String contextPath = formatContextPath(this.getKeyAuthContextPath());
        return CommonConstants.HTTPS_PREFIX + this.getKeyAuthHost() + port + contextPath;
    }

    public String getSessionAuthUrl() {
        String port = (this.getSessionAuthPort() == 443) ? "" : ":" + this.getSessionAuthPort();
        String contextPath = formatContextPath(this.getSessionAuthContextPath());
        return CommonConstants.HTTPS_PREFIX + this.getSessionAuthHost() + port + contextPath;
    }

    public String getDatafeedIdFilePath() {
        if (datafeedIdFilePath == null || datafeedIdFilePath.isEmpty()) {
            return "." + File.separator;
        }
        if (!datafeedIdFilePath.endsWith(File.separator)) {
            return datafeedIdFilePath + File.separator;
        }
        return datafeedIdFilePath;
    }

    private String formatContextPath(String contextPath) {
        String formattedPath = (contextPath == null) ? "" : contextPath;
        if (!formattedPath.equals("") && formattedPath.charAt(0) != '/') {
            formattedPath =  "/" + formattedPath;
        }
        if (!formattedPath.equals("") && formattedPath.endsWith("/")) {
            formattedPath = formattedPath.substring(0, formattedPath.length() - 1);
        }
        return formattedPath;
    }

    public int getDatafeedEventsThreadpoolSize() {
        return this.datafeedEventsThreadpoolSize > 0 ? this.datafeedEventsThreadpoolSize : 5;
    }

    public int getDatafeedEventsErrorTimeout() {
        return this.datafeedEventsErrorTimeout > 0 ? this.datafeedEventsErrorTimeout : 30;
    }

}
