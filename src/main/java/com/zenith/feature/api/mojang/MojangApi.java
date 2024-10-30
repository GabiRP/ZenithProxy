package com.zenith.feature.api.mojang;

import com.zenith.feature.api.Api;
import com.zenith.feature.api.mojang.model.MojangProfileResponse;

import java.util.Optional;

public class MojangApi extends Api {
    public static final MojangApi INSTANCE = new MojangApi();

    public MojangApi() {
        super("https://api.mojang.com");
    }

    public Optional<MojangProfileResponse> getProfile(final String username) {
        return get("/users/profiles/minecraft/" + username, MojangProfileResponse.class);
    }
}
