package com.symphony.bdk.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.symphony.bdk.core.config.exception.BdkConfigException;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class BdkConfigParserTest {

    @BeforeEach
    void tearDown() {
        System.clearProperty("my.property");
        System.clearProperty("recursive");
    }

    @Test
    void parseJsonConfigTest() throws BdkConfigException {
        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config.json");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("tibot", jsonNode.at("/bot/username").asText());
    }

    @Test
    void parseJsonConfigWithPropertyTest() throws BdkConfigException {
        System.setProperty("my.property", "propvalue");

        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_properties.json");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);

        assertEquals("${escaped}", jsonNode.at("/host").asText());
        assertEquals("propvalue/privatekey.pem", jsonNode.at("/bot/privateKeyPath").asText());
        assertEquals("default/value/privatekey.pem", jsonNode.at("/app/privateKeyPath").asText());
    }

    @Test
    void parseJsonConfigWithPropertyContainingSpecialCharsTest() throws BdkConfigException {
        System.setProperty("my.property", "propvalue:\"\n");

        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_properties.json");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("propvalue:\"\n/privatekey.pem", jsonNode.at("/bot/privateKeyPath").asText());
    }

    @Test
    void parseJsonConfigWithRecursivePropertyShouldNotWorkTest() throws BdkConfigException {
        System.setProperty("recursive", "my.property");
        System.setProperty("my.property", "propvalue");

        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_recursive_props.json");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("${${recursive}}/privatekey.pem", jsonNode.at("/bot/privateKeyPath").asText());
    }

    @Test
    void parseJsonConfigWithUndefinedPropertyTest() throws BdkConfigException {
        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_properties.json");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("${my.property}/privatekey.pem", jsonNode.at("/bot/privateKeyPath").asText());
    }

    @Test
    void parseYamlConfigTest() throws BdkConfigException {
        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config.yaml");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("tibot", jsonNode.at("/bot/username").asText());
    }

    @Test
    void parseYamlConfigWithPropertyTest() throws BdkConfigException {
        System.setProperty("my.property", "propvalue");

        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_properties.yaml");
        JsonNode jsonNode = BdkConfigParser.parse(inputStream);
        assertEquals("propvalue/privatekey.pem", jsonNode.at("/bot/privateKey/path").asText());
    }

    @Test
    void parseYamlConfigWithPropertiesInArray() throws BdkConfigException {
        System.setProperty("my.property", "agent-lb.acme.org");

        InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/config_lb_properties.yaml");
        final JsonNode loadBalancing = BdkConfigParser.parse(inputStream).at("/agent/loadBalancing");

        assertEquals("roundRobin", loadBalancing.at("/mode").asText());
        assertEquals(true, loadBalancing.at("/stickiness").asBoolean());

        assertEquals("agent1.acme.org", loadBalancing.at("/nodes/0/host").asText());
        assertEquals(1234, loadBalancing.at("/nodes/0/port").asInt());

        assertEquals("agent-lb.acme.org", loadBalancing.at("/nodes/1/host").asText());
    }

    @Test
    void parseInvalidConfigTest() {
        BdkConfigException exception = assertThrows(BdkConfigException.class, () -> {
            InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/invalid_config.html");
            BdkConfigParser.parse(inputStream);
        });
        assertEquals("Config file is not in a valid format", exception.getMessage());
    }

    @Test
    void parseInvalidYamlConfigTest() {
        BdkConfigException exception = assertThrows(BdkConfigException.class, () -> {
            InputStream inputStream = BdkConfigLoaderTest.class.getResourceAsStream("/config/invalid_config.yaml");
            BdkConfigParser.parse(inputStream);
        });
        assertEquals("Config file is not in a valid format", exception.getMessage());
    }
}
