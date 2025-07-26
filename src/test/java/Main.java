import top.jgroup.SpotifyAuthService;
import top.jgroup.SpotifyClient;
import top.jgroup.SpotifyPlayerService;
import top.jgroup.models.TokenResponse;
import top.jgroup.models.SpotifyTrackInfo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        }

        String clientId = props.getProperty("CLIENT_ID");
        String clientSecret = props.getProperty("CLIENT_SECRET");
        String redirectUri = props.getProperty("REDIRECT_URI");

        SpotifyClient client = new SpotifyClient(clientId, clientSecret, redirectUri);

        SpotifyAuthService authService = client.getAuthService();
        SpotifyPlayerService playerService = client.getPlayerService();

        try {
            TokenResponse tokenResponse = null;
            if (props.containsKey("REFRESH_TOKEN")) {
                String refreshToken = props.getProperty("REFRESH_TOKEN");
                tokenResponse = authService.refreshAccessToken(refreshToken);
                System.out.println("Access token refreshed: " + tokenResponse.getAccessToken());
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter the authorization code you received from Spotify:");
                String authorizationCode = scanner.nextLine();
                tokenResponse = authService.exchangeCodeForToken(authorizationCode);

                System.out.println("Access token: " + tokenResponse.getAccessToken());
                System.out.println("Refresh token: " + tokenResponse.getRefreshToken());

                props.setProperty("REFRESH_TOKEN", tokenResponse.getRefreshToken());

                try (FileOutputStream output = new FileOutputStream("config.properties")) {
                    props.store(output, "Updated with refresh token");
                }

            }

            if (!playerService.isAccessTokenValid(tokenResponse.getAccessToken())) {
                tokenResponse = authService.refreshAccessToken(tokenResponse.getRefreshToken());
                System.out.println("Token refreshed");
            }

            SpotifyTrackInfo currentTrack = playerService.getCurrentTrack(tokenResponse.getAccessToken());

            if (currentTrack != null) {
                System.out.println("Now playing: " + currentTrack);
            } else {
                System.out.println("No track is currently playing");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
