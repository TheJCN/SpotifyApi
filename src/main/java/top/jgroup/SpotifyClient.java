package top.jgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

public class SpotifyClient {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SpotifyClient(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public SpotifyAuthService getAuthService() {
        return new SpotifyAuthService(clientId, clientSecret, redirectUri, httpClient, objectMapper);
    }

    public SpotifyPlayerService getPlayerService() {
        return new SpotifyPlayerService(httpClient, objectMapper);
    }
}

