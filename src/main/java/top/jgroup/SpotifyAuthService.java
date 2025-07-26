package top.jgroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import top.jgroup.models.TokenResponse;

import java.io.IOException;

public class SpotifyAuthService {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SpotifyAuthService(String clientId, String clientSecret, String redirectUri,
                              OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public TokenResponse exchangeCodeForToken(String code) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to exchange code for token: " + response);
            }

            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            return parseTokenResponse(jsonNode);
        }
    }

    public TokenResponse refreshAccessToken(String refreshToken) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to refresh access token: " + response);
            }

            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            return parseTokenResponse(jsonNode, refreshToken);
        }
    }

    private TokenResponse parseTokenResponse(JsonNode jsonNode) {
        return parseTokenResponse(jsonNode, null);
    }

    private TokenResponse parseTokenResponse(JsonNode jsonNode, String oldRefreshToken) {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(jsonNode.get("access_token").asText());
        tokenResponse.setExpiresIn(jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 0);

        if (jsonNode.has("refresh_token")) {
            tokenResponse.setRefreshToken(jsonNode.get("refresh_token").asText());
        } else {
            tokenResponse.setRefreshToken(oldRefreshToken);
        }
        return tokenResponse;
    }
}
