package com.asia.token;

import org.json.*;
import spark.Request;
import java.time.Instant;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JsonTokenStore implements TokenStore {

    @Override
    public String create(Request request, Token token) {
        var json = new JSONObject();
        json.put("sub", token.username);
        json.put("exp", token.expiry.getEpochSecond());
        json.put("attrs", token.attributes);
        System.out.println("JsonTokenStore.create.json: " + json);
        var jsonBytes = json.toString().getBytes(UTF_8);
        System.out.println("JsonTokenStore.create.jsonBytes: " + jsonBytes);
        System.out.println("JsonTokenStore.create.Base64url.encode: " + Base64url.encode(jsonBytes));

        return Base64url.encode(jsonBytes);
    }

    @Override
    public Optional<Token> read(Request request, String tokenId) {
        System.out.println(">JsonTokenStore.read().tokenId: " + tokenId);
        try {
            var decoded = Base64url.decode(tokenId);
            var json = new JSONObject(new String(decoded, UTF_8));
            var expiry = Instant.ofEpochSecond(json.getInt("exp"));
            var username = json.getString("sub");
            var attrs = json.getJSONObject("attrs");
            var token = new Token(expiry, username);
            for (var key : attrs.keySet()) {
                token.attributes.put(key, attrs.getString(key));
            }

            return Optional.of(token);
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    @Override
    public void revoke(Request request, String tokenId) {
        // TODO
    }

    public static void main(String[] args) {

        var json = new JSONObject();
        json.put("sub", "usertest");
        json.put("exp", Instant.now());
        json.put("attrs", "");
        System.out.println(">JsonTokenStore.main().json: " + json.toString());
        var jsonBytes = json.toString().getBytes(UTF_8);
        System.out.println(">JsonTokenStore.main().jBytes_array: ");
        for (int i = 0; i < jsonBytes.length; i++) {
            System.out.print(jsonBytes[i] + "|");
        }

        String tokenEncoded = Base64url.encode(jsonBytes);
        System.out.println(">JsonTokenStore.main().Base64url.encoded: " + tokenEncoded);
        System.out.println(">JsonTokenStore.main().DECODE #####");
        byte[] arrayTokenDecoded = Base64url.decode(tokenEncoded);
        System.out.println(">JsonTokenStore.main().byte decode:");
        for (int i = 0; i < arrayTokenDecoded.length; i++) {
            System.out.print(arrayTokenDecoded[i] + "|");
        }
        System.out.println();
        String stringDecoded = new String(arrayTokenDecoded, UTF_8);
        System.out.println();
        System.out.println(">JsonTokenStore.main().StringDecoded: " + stringDecoded);

    }

}
