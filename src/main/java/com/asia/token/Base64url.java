package com.asia.token;


import java.util.Base64;

public class Base64url {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    public static String encode(byte[] data) {

        System.out.println("Base64URL.encode.data byte[]: "+data);
        System.out.println("ENCODE: "+encoder.encodeToString(data));
        return encoder.encodeToString(data);
    }

    public static byte[] decode(String encoded) {
        System.out.println("Base64URL.decode.String: "+encoded);
        System.out.println("DECODE: "+decoder.decode(encoded));

        return decoder.decode(encoded);
    }
}
