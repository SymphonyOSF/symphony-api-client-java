package com.symphony.bdk.core.config;

import com.symphony.bdk.core.config.exception.BdkConfigException;
import com.symphony.bdk.core.config.legacy.LegacyConfigMapper;
import com.symphony.bdk.core.config.legacy.model.LegacySymConfig;
import com.symphony.bdk.core.config.model.BdkClientConfig;
import com.symphony.bdk.core.config.model.BdkConfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public class BdkConfigLoader {

    private static final ObjectMapper JSON_MAPPER = new JsonMapper();

    /**
     * Load BdkConfig from a file path
     *
     * @param configPath Path of the config file
     *
     * @return Symphony Bot Configuration
     */
    public static BdkConfig loadFromFile(String configPath) throws BdkConfigException {
        try {
            File file = new File(configPath);
            InputStream inputStream = new FileInputStream(file);
            return loadFromInputStream(inputStream);
        } catch (FileNotFoundException e) {
            throw new BdkConfigException("Config file is not found");
        }
    }

    /**
     * Load BdkConfig from an InputStream
     *
     * @param inputStream InputStream
     *
     * @return Symphony Bot Configuration
     */
    public static BdkConfig loadFromInputStream(InputStream inputStream) throws BdkConfigException {
        if (inputStream != null) {
            JsonNode jsonNode = BdkConfigParser.parse(inputStream);
            if (jsonNode != null) {
                JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                if (jsonNode.at("/botUsername").isMissingNode()) {
                    BdkConfig config = JSON_MAPPER.convertValue(jsonNode, BdkConfig.class);
                    return setUpGlobalValueConfig(config);
                } else {
                    LegacySymConfig legacySymConfig = JSON_MAPPER.convertValue(jsonNode, LegacySymConfig.class);
                    return LegacyConfigMapper.map(legacySymConfig);
                }
            }
        }
        return null;
    }

    /**
     * Load BdkConfig from a classpath
     *
     * @param configPath Classpath to config file
     *
     * @return Symphony Bot Configuration
     */
    public static BdkConfig loadFromClasspath(String configPath) throws BdkConfigException {
        InputStream inputStream = BdkConfigLoader.class.getResourceAsStream(configPath);
        if (inputStream != null) {
            return loadFromInputStream(inputStream);
        }
        throw new BdkConfigException("Config file is not found");
    }

    private static BdkConfig setUpGlobalValueConfig(BdkConfig config) {
      config.setPod(setUpClientConfig(config, config.getPod()));
      config.setAgent(setUpClientConfig(config, config.getAgent()));
      config.setKeyManager(setUpClientConfig(config, config.getKeyManager()));
      config.setSessionAuth(setUpClientConfig(config, config.getSessionAuth()));
      return config;
    }

    private static BdkClientConfig setUpClientConfig(BdkConfig config, BdkClientConfig clientConfig) {
      if (clientConfig.getHost() == null) {
        clientConfig.setHost(config.getHost());
      }
      if (clientConfig.getPort() == null) {
        clientConfig.setPort(config.getPort());
      }
      if (clientConfig.getScheme() == null) {
        clientConfig.setScheme(config.getScheme());
      }
      if (clientConfig.getContext() == null) {
        clientConfig.setContext(config.getContext());
      }
      return clientConfig;
    }
}
