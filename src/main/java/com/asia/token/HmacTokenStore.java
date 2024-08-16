package com.asia.token;

import spark.Request;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public class HmacTokenStore implements SecureTokenStore {

    private final TokenStore delegate;
    private final Key macKey;

    private HmacTokenStore(TokenStore delegate, Key macKey) {
        this.delegate = delegate;
        this.macKey = macKey;
        System.out.println(">>>HmacTokenStore().macKey.algorithm: " + macKey.getAlgorithm());
    }

    public static SecureTokenStore wrap(ConfidentialTokenStore store, Key macKey) {
        return new HmacTokenStore(store, macKey);
    }

    public static AuthenticatedTokenStore wrap(TokenStore store, Key macKey) {
        return new HmacTokenStore(store, macKey);
    }

    @Override
    public String create(Request request, Token token) {
        System.out.println(">>>HmacTokenStore.create() <<<");
        var tokenId = delegate.create(request, token);
        System.out.println(">>>HmacTokenStore.create().tokenId: " + tokenId);
        var tag = hmac(tokenId);
        System.out.println(">>>HmacTokenStore.create().tokenId.Base64urlEncode(tag): " + tokenId + '.' + Base64url.encode(tag));
        return tokenId + '.' + Base64url.encode(tag);
    }

    private byte[] hmac(String tokenId) {
        System.out.println(">>>HmacTokenStore.hmac().tokenId: " + tokenId);
        try {
            Mac mac = Mac.getInstance(macKey.getAlgorithm());
            System.out.println(">>>HmacTokenStore.hmac().macKey.algorithm: " + macKey.getAlgorithm());
            mac.init(macKey);

            System.out.println(">>>HmacTokenStore.hamac().tag bytes: ");
            byte[] temp = mac.doFinal(tokenId.getBytes(StandardCharsets.UTF_8));
            System.out.println("<--- hmac.length: " + temp.length + " --->");
            for (int i = 0; i < temp.length; i++) {
                System.out.print(temp[i] + ",");
            }
            System.out.println();
            //s.getBytes(StandardCharsets.UTF_8).length

            return mac.doFinal(tokenId.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Token> read(Request request, String tokenId) {
        System.out.println(">HmacTokenStore.read().tokenId: " + tokenId);
        var index = tokenId.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        }
        var realTokenId = tokenId.substring(0, index);
        var provided = Base64url.decode(tokenId.substring(index + 1));
        var computed = hmac(realTokenId);

        if (!MessageDigest.isEqual(provided, computed)) {
            return Optional.empty();
        }

        return delegate.read(request, realTokenId);
    }

    @Override
    public void revoke(Request request, String tokenId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
