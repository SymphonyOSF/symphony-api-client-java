package clients.symphony.api;

import clients.ISymClient;
import clients.symphony.api.constants.PodConstants;
import exceptions.SymClientException;
import exceptions.UnauthorizedException;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Presence;
import model.UserPresence;

public class PresenceClient extends APIClient {
    private ISymClient botClient;

    public PresenceClient(ISymClient client) {
        botClient = client;
    }

    public UserPresence getUserPresence(Long userId, boolean local) throws SymClientException {
        Invocation.Builder builder = botClient.getPodClient()
            .target(botClient.getConfig().getPodUrl())
            .path(PodConstants.GETUSERPRESENCE.replace("{uid}", Long.toString(userId)))
            .queryParam("local", local)
            .request(MediaType.APPLICATION_JSON)
            .header("sessionToken", botClient.getSymAuth().getSessionToken());

        try (Response response = builder.get()) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                try {
                    handleError(response, botClient);
                } catch (UnauthorizedException ex) {
                    return getUserPresence(userId, local);
                }
                return null;
            }
            return response.readEntity(UserPresence.class);
        }
    }

    public UserPresence setPresence(Presence status) throws SymClientException {
        return setPresence(status.toString());
    }

    public UserPresence setPresence(String status) throws SymClientException {
        Entity entity = Entity.entity(new Category(status), MediaType.APPLICATION_JSON);

        Invocation.Builder builder = botClient.getPodClient()
            .target(botClient.getConfig().getPodUrl())
            .path(PodConstants.SETPRESENCE)
            .request(MediaType.APPLICATION_JSON)
            .header("sessionToken", botClient.getSymAuth().getSessionToken());

        try (Response response = builder.post(entity)) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                try {
                    handleError(response, botClient);
                } catch (UnauthorizedException ex) {
                    return setPresence(status);
                }
                return null;
            }
            return response.readEntity(UserPresence.class);
        }
    }

    public void registerInterestExtUser(List<Long> userIds) throws SymClientException {
        Invocation.Builder builder = botClient.getPodClient()
            .target(botClient.getConfig().getPodUrl())
            .path(PodConstants.REGISTERPRESENCEINTEREST)
            .request(MediaType.APPLICATION_JSON)
            .header("sessionToken", botClient.getSymAuth().getSessionToken());

        try (Response response = builder.post(Entity.entity(userIds, MediaType.APPLICATION_JSON))) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                try {
                    handleError(response, botClient);
                } catch (UnauthorizedException ex) {
                    registerInterestExtUser(userIds);
                }
            }
        }
    }

    private class Category {
        private String category;

        public Category() {}

        public Category(String category) {
            this.category = category;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
