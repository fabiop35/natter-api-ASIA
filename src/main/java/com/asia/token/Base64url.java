package com.asia.token;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

public class Base64url {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    public static String encode(byte[] data) {
        System.out.println();
        System.out.println(">Base64url.encode().array_values: ");
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + ",");
        }
        System.out.println();

        System.out.println(">Base64url.encode().ENCODED_Array_To_String: " + encoder.encodeToString(data));
        System.out.println();

        return encoder.encodeToString(data);
    }

    public static byte[] decode(String encoded) {
        System.out.println(">Base6url.decode().String encoded: " + encoded);
        System.out.println(">Base64url.decode().arrayElements values: ");
        byte[] decoArr = decoder.decode(encoded);
        for (int i = 0; i < decoArr.length; i++) {
            System.out.print(decoArr[i] + "|");
        }
        System.out.println();
        System.out.println(">Base64url.decode().arrayElements values: ");
        for (int i = 0; i < decoArr.length; i++) {
            System.out.print(decoArr[i] + "|");
        }
        System.out.println();

        return decoArr;
    }

    public static void main(String[] args) {
        System.out.println("Base64url.main().Default charset: " + Charset.defaultCharset());
        System.out.println("Main.encode: ");
        //System.out.println(encode("ahojľščáýíô".getBytes()));
        //1. converts a String into an array[] of bytes
        encode("hfpv".getBytes());

        //System.out.println("decoded: " + new String(decode(encode("ahojľščáýíô".getBytes()))));
        /*byte[] deco = decode("aGZwdg"); 
        for (int i = 0; i < deco.length; i++) {
            System.out.print((char)deco[i]);  
        }
        System.out.println();*/
        decode("aGZwdg");
        //System.out.println(Arrays.toString(decode("aGZwdg==")) );
        //
        byte[] bytearr = {1, 2, 3, 4};
        System.out.println("length: " + bytearr.length);
        /*for(int i=0; i<bytearr.length;i++){
           System.out.print(bytearr[i]);
        }
        System.out.println("CHAR");
        for(int i=0; i<bytearr.length;i++){
           System.out.print((char)bytearr[i]+",");
        }*/

        String encodedText = encode(bytearr);
        System.out.println("encodedText: " + encodedText);
        //web MDEyMzQ

    }
}
