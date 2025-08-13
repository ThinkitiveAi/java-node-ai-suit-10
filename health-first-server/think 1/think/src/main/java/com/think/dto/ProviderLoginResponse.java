package com.think.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLoginResponse {
    private boolean success;
    private String message;
    private LoginData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginData {
        private String access_token;
        private long expires_in;
        private String token_type;
        private ProviderResponse provider;
    }
    
    public static ProviderLoginResponse success(String token, long expiresIn, ProviderResponse provider) {
        LoginData data = new LoginData();
        data.setAccess_token(token);
        data.setExpires_in(expiresIn);
        data.setToken_type("Bearer");
        data.setProvider(provider);
        
        return new ProviderLoginResponse(true, "Login successful", data);
    }
}
