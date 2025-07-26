package top.jgroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import top.jgroup.models.SpotifyTrackInfo;

import java.io.IOException;

public class SpotifyPlayerService {

    private static final String CURRENT_TRACK_URL = "https://api.spotify.com/v1/me/player/currently-playing";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SpotifyPlayerService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public SpotifyTrackInfo getCurrentTrack(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CURRENT_TRACK_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get current track: " + response);
            }
            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode itemNode = rootNode.path("item");

            if (itemNode.isMissingNode() || itemNode.isNull()) {
                return null;
            }

            String trackName = itemNode.path("name").asText("Unknown track");
            JsonNode artistsNode = itemNode.path("artists");
            String artistName = "Unknown artist";

            if (artistsNode.isArray() && !artistsNode.isEmpty()) {
                artistName = artistsNode.get(0).path("name").asText("Unknown artist");
            }

            JsonNode albumNode = itemNode.path("album");
            JsonNode imagesNode = albumNode.path("images");
            String albumCoverUrl = null;
            if (imagesNode.isArray() && !imagesNode.isEmpty()) {
                albumCoverUrl = imagesNode.get(0).path("url").asText(null);
            }

            return new SpotifyTrackInfo(trackName, artistName, albumCoverUrl);
        }
    }

    public boolean isAccessTokenValid(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(CURRENT_TRACK_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }
}
