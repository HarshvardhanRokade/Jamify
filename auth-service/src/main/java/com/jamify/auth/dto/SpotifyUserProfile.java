package com.jamify.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SpotifyUserProfile {

    @JsonProperty("id")
    private String id;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("product")
    private String product;

    @JsonProperty("images")
    private List<SpotifyImage> images;

    @Data
    public static class SpotifyImage{

        @JsonProperty("url")
        private String url;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("width")
        private Integer width;
    }

    public String getAvatarUrl(){
        if(images != null && !images.isEmpty()){
            return images.get(0).url;
        }
        return null;
    }

    public boolean isPremium(){
        return "premium".equals(product);
    }
}
