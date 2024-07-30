package com.asia;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.Date;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

//import com.nimbusds.jose.JW
//import com.nimbusds.jose.jwk.*;
//import com.nimbusds.jose.jwk.gen.*;
public class JWKOctetSequenceKey {

    public static void main(String[] args) throws JOSEException, NoSuchAlgorithmException {
        System.out.println(">>>INI: JWKOctectSequenceKey");

        OctetSequenceKey jwk = new OctetSequenceKeyGenerator(256)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.HS256)
                //.issueTime(new Date())
                .generate();

        System.out.println(jwk);
        //OUTPUT: {"kty":"oct","kid":"e9f510ec-513b-49be-9adf-258b27cc17b3","k":"8D3JUY7GBTLfStB-R1aarxF95iYdsJr53M35ehI4fPM","alg":"HS256"}
        /*
        {
 "kty": "oct",
 "kid": "e9f510ec-513b-49be-9adf-258b27cc17b3",
 "k": "8D3JUY7GBTLfStB-R1aarxF95iYdsJr53M35ehI4fPM",
 "alg": "HS256"
}
         */
        System.out.println("### Generate a secret key with 256 bits ###");
        // Generate a secret key with 256 bits
        SecretKey hmacKey = KeyGenerator.getInstance("HmacSha256").generateKey();

        System.out.println("HMAC Key: " + hmacKey);

        // Convert to JWK format
        JWK jwk2 = new OctetSequenceKey.Builder(hmacKey)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.HS256)
                .issueTime(new Date())
                .build();

        System.out.println(jwk2);

    }
}
