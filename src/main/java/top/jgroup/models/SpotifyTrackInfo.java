package top.jgroup.models;

import lombok.Data;

@Data
public class SpotifyTrackInfo {
    private final String trackName;
    private final String artistName;
    private final String albumCoverUrl;

    public SpotifyTrackInfo(String trackName, String artistName, String albumCoverUrl) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumCoverUrl = albumCoverUrl;
    }
}