# Symphony API Client for Java

[![CircleCI](https://circleci.com/gh/SymphonyPlatformSolutions/symphony-api-client-java.svg?style=shield)](https://circleci.com/gh/SymphonyPlatformSolutions/symphony-api-client-java) [![Maven Central](https://img.shields.io/maven-central/v/com.symphony.platformsolutions/symphony-api-client-java)](https://mvnrepository.com/artifact/com.symphony.platformsolutions/symphony-api-client-java) [![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](https://opensource.org/licenses/MIT) [![Email](https://img.shields.io/static/v1?label=contact&message=email&color=darkgoldenrod)](mailto:platformsolutions@symphony.com?subject=Java%20SDK)

This client library facilitates connectivity to a Symphony pod and simplifies the creation of bots and
extension applications using the REST API. It provides the necessary API bindings, centralised configuration,
authentication, data feed / firehose event loops, message parsing and agent server load balancing.

## Installation
### Maven
```xml
<dependency>
    <groupId>com.symphony.platformsolutions</groupId>
    <artifactId>symphony-api-client-java</artifactId>
    <version>[1.1.0,)</version>
</dependency>
```

### Gradle
```groovy
compile: 'com.symphony.platformsolutions:symphony-api-client-java:1.1.+'
```

## Installation via Bot Generator
To generate example projects that use this library, please use the [generator-symphony](https://github.com/SymphonyPlatformSolutions/generator-symphony) command line interface.
```bash
$ npm i -g yo generator-symphony
$ yo symphony
```
The generator creates projects demonstrating bot and application workflows using features such as:
* Spring Boot
* Camunda BPM
* Apache NLP

## Configuration
Create a config.json file in your project which includes the following properties.
You can exclude sections irrelevant to your project. For example, in a bot project
authenticating via RSA and connecting directly to the pod, agent and key manager, you
can exclude the bot certificate section, all extension app sections and all optional sections.

```json5
{
    // Mandatory section
    "sessionAuthHost": "my-company-name-api.symphony.com",
    "sessionAuthPort": 8444,
    "keyAuthHost": "my-company-name-api.symphony.com",
    "keyAuthPort": 8444,
    "podHost": "my-company-name.symphony.com",
    "podPort": 443,
    "agentHost": "my-company-name.symphony.com",
    "agentPort": 443,
    
    // For bots only
    "botUsername": "my-bot-name",
    "botEmailAddress": "bot@company.com",
    // For bots using RSA authentication
    "botPrivateKeyPath": "/path/to/rsa/private-key/",
    "botPrivateKeyName": "bot-private-key.pem",
    // For bots using certificate authentication
    "botCertPath": "/path/to/bot-cert/",
    "botCertName": "bot-cert.p12",
    "botCertPassword": "bot-cert-password",
    
    // For extension apps only
    "appId": "app-id",
    // For extension apps using RSA authentication
    "appPrivateKeyPath": "/path/to/rsa-private-key/",
    "appPrivateKeyName": "app-private-key.pkcs8",
    // For extension apps using certificate authentication
    "appCertPath": "/path/to/app-cert/",
    "appCertName": "app-cert.p12",
    "appCertPassword": "app-cert-password",
    
    // Optional: If the connection to the pod (but not the agent) needs to run through a proxy
    "podProxyURL": "http://localhost:3128",
    "podProxyUsername": "proxy-username",
    "podProxyPassword": "proxy-password",
    
    // Optional: If the connection to both the pod and the agent needs to run through a proxy
    //           Do not include the podProxy properties if using this
    "proxyURL": "http://localhost:3128",
    "proxyUsername": "proxy-username",
    "proxyPassword": "proxy-password",
    
    // Optional: If the connection to the key manager needs to run through a proxy
    "keyManagerProxyURL": "http://localhost:3128",
    "keyManagerProxyUsername": "proxy-username",
    "keyManagerProxyPassword": "proxy-password",
    
    // Optional: If the SSL connection to any endpoint uses private or self-signed certificates
    "truststorePath": "/path/to/store/truststore.pks",
    "truststorePassword": "changeit",
    
    // Optional: To modify the default datafeed handling properties
    "datafeedEventsThreadpoolSize": 5,
    "datafeedEventsErrorTimeout": 30,
    "reuseDatafeedID": true,
    
    // Optional: Request filter pattern to verify JWT
    "authenticationFilterUrlPattern": "/v1/",
    
    // Optional: If custom URI schemes need to be supported by MessageML parser 
                 By setting this property, the default schemes (http and https) will be overridden
    "supportedUriSchemes": ["http", "https", "customScheme"],
}
```

## Getting Started
### Automatic bootstrap
```java
// Load configuration, authenticate and get bot client
// Uses file in working directory if exists, else uses resource in classpath
SymBotClient botClient = SymBotClient.initBotRsa("config.json"); // RSA-based auth or
SymBotClient botClient = SymBotClient.initBot("config.json");    // Cert-based auth

// Link datafeed listeners 
botClient.getDatafeedEventsService().addListeners(
    new IMListenerImpl(botClient),
    new RoomListenerImpl(botClient)
);
```

### Alternative: Manually load configuration and authentication
```java
// Load configuration
// Uses file in working directory if exists, else uses resource in classpath
SymConfig config = SymConfigLoader.loadConfig("config.json");

// Authenticate
ISymAuth botAuth = new SymBotRSAAuth(config); // RSA-based auth or
ISymAuth botAuth = new SymBotAuth(config);    // Cert-based auth
botAuth.authenticate();

// Get bot client
SymBotClient botClient = SymBotClient.initBot(config, botAuth);

// Link datafeed listeners
botClient.getDatafeedEventsService().addListeners(
    new IMListenerImpl(botClient),
    new RoomListenerImpl(botClient)
);
```

### Optional: Custom session and key-manager configurations
```java
ClientConfig sessionAuthClientConfig = new ClientConfig();
ClientConfig kmAuthClientConfig = new ClientConfig();
ISymAuth botAuth = new SymBotRSAAuth(config, sessionAuthClientConfig, kmAuthClientConfig); // RSA-based or
ISymAuth botAuth = new SymBotAuth(config, sessionAuthClientConfig, kmAuthClientConfig);    // Certificate-based
botAuth.authenticate();
```
        
### Optional: Request filter
Provides a filter component to validate requests based on their JWT. To enable this feature, it's required that you instantiate the AuthenticationFilter (see example below) and a URL pattern which the filter will be applied to.
```java
AuthenticationFilter filter = new AuthenticationFilter(symExtensionAppRSAAuth, config);
```

## Example Project
### Main Class
```java
public class MyBot {
    private static SymBotClient botClient;

    public static void main(String [] args) {
        new MyBot();
    }
    
    public MyBot() {
        botClient = SymBotClient.initBotRsa("config.json");
        botClient.getDatafeedEventsService().addListeners(
            new IMListenerImpl(),
            new RoomListenerImpl(),
            new ElementsListenerImpl()
        );
    }

    public static SymBotClient getBotClient() {
        return botClient;
    }
}
```

### IMListener Implementation
```java
public class IMListenerImpl implements IMListener {
    public void onIMMessage(InboundMessage message) {
        SymBotClient bot = MyBot.getBotClient();
        String streamId = message.getStream().getStreamId();
        String messageOut = String.format("Hello %s!", message.getUser().getDisplayName());
        bot.getMessagesClient().sendMessage(streamId, new OutboundMessage(messageOut));
    }

    public void onIMCreated(Stream stream) {}
}
```

### RoomListener Implementation
```java
public class RoomListenerImpl implements RoomListener {
    private SymBotClient bot;

    public RoomListenerImpl() {
        this.bot = MyBot.getBotClient();
    }

    public void onRoomMessage(InboundMessage message) {
        String streamId = message.getStream().getStreamId();
        String firstName = message.getUser().getFirstName();
        String messageOut = String.format("Hello %s!", firstName);
        this.bot.getMessagesClient().sendMessage(streamId, new OutboundMessage(messageOut));
    }

    public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {
        String streamId = userJoinedRoom.getStream().getStreamId();
        String firstName = userJoinedRoom.getAffectedUser().getFirstName();
        String messageOut = String.format("Welcome %s!", firstName);
        this.bot.getMessagesClient().sendMessage(streamId, new OutboundMessage(messageOut));
    }

    public void onRoomCreated(RoomCreated roomCreated) {}
    public void onRoomDeactivated(RoomDeactivated roomDeactivated) {}
    public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {}
    public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {}
    public void onRoomReactivated(Stream stream) {}
    public void onRoomUpdated(RoomUpdated roomUpdated) {}
    public void onUserLeftRoom(UserLeftRoom userLeftRoom) {}
}
```

### ElementsListener Implementation
```java
public class ElementsListenerImpl implements ElementsListener {
    public void onElementsAction(User initiator, SymphonyElementsAction action) {
        SymBotClient bot = MyBot.getBotClient();
        Map<String, Object> formValues = action.getFormValues();
        String input = (String) formValues.get("input-name");

        String messageOut = String.format("Hi %s! You entered: %s", initiator.getFirstName(), input);
        bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage(messageOut));
    }
}
```

### Symphony Elements FormBuilder
```java
String formML = FormBuilder.builder("sec-finder-form")
    .addHeader(6, "Security Reference")
    .addTextField("secCode", "", "Enter a code..", true, false, 1, 15)
    .addHeader(6, "Assigned To:")
    .addPersonSelector("assignedTo", "Assign to..", false)
    .addHeader(6, "Trade Status:")
    .addRadioButton("status", "Pending", "pending", true)
    .addRadioButton("status", "Confirmed", "confirmed", false)
    .addRadioButton("status", "Settled", "settled", false)
    .addHeader(6, "Desk:")
    .addDropdownMenu("assetClass", false, Arrays.asList(
        new DropdownMenuOption("eq", "Equities", true),
        new DropdownMenuOption("fi", "Credit", false),
        new DropdownMenuOption("fx", "FX", false),
        new DropdownMenuOption("rates", "Rates", false)
    ))
    .addCheckBox("deliverable", "Non-Deliverable?", "nd", false)
    .addHeader(6, "Remarks:")
    .addTextArea("remarks", "", "Enter your remarks..", false)
    .addButton("confirm", "Confirm", FormButtonType.ACTION)
    .addButton("reset", "Reset", FormButtonType.RESET)
    .formatElement();

botClient.getMessagesClient().sendMessage(streamId, new OutboundMessage(formML));
```

## Advanced Configuration for Load Balancing
Create an additional configuration file to support load balancing.
There are 3 supported types:
* Round-Robin
* Random
* External

Round-robin and random load balancing are managed by this library based on the servers provided in the agentServers array.

External load-balancing is managed by an external DNS, cloud provider or hardware-based solution. List only one load balancer frontend hostname in the agentServers array (subsequent server entries for the external method are ignored).

**Note:** that this method requires all underlying agent servers to implement an additional `host.name` switch with the current server's FQDN in their `startup.sh` script.

```bash
exec java $JAVA_OPTS -Dhost.name=sym-agent-01.my-company.com
``` 

There is also support for sticky sessions, which should be true for any bot that requires the datafeed loop. Using non-sticky load-balanced configuration with a datafeed loop will result in unexpected results.
```json5
{
    "loadBalancing": {
        "method": "random", // or roundrobin or external
        "stickySessions": true
    },
    "agentServers": [
        "sym-agent-01.my-company.com",
        "sym-agent-02.my-company.com",
        "sym-agent-03.my-company.com"
    ]
}
```

### Loading advanced configuration
#### Automatic bootstrap
```java
// Load configuration, authenticate and get bot client
// Uses files in working directory if they exist, else uses resources in classpath
SymBotClient botClient = SymBotClient.initBotLoadBalancedRsa("config.json", "lb-config.json"); // RSA-based auth or
SymBotClient botClient = SymBotClient.initBotLoadBalanced("config.json", "lb-config.json");    // Cert-based auth
```

#### Alternative: Manually load configuration and authentication
```java
// Load configuration
// Uses files in working directory if they exist, else uses resources in classpath
SymConfig config = SymConfigLoader.loadConfig("config.json");
SymLoadBalancedConfig lbConfig = SymConfigLoader.loadLoadBalancerConfig("lb-config.json");

// Authenticate
ISymAuth botAuth = new SymBotRSAAuth(config); // RSA-based auth or
ISymAuth botAuth = new SymBotAuth(config);    // Cert-based auth
botAuth.authenticate();

// Get bot client
SymBotClient botClient = SymBotClient.initBot(config, botAuth, lbConfig);
```
