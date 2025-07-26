package top.jgroup.models;

import lombok.Data;

@Data
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
