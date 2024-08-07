package com.asia;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

public class ShowKey {

    public static void main(String[] args) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

        String fileName = "keystore.p12";
        char[] password = "changeit".toCharArray();
        String alias = "hmac-key";

        //KeyStore ks = KeyStore.getInstance("JCEKS");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(fileName)) {
            ks.load(fis, password);
            SecretKey secretKey = (SecretKey) ks.getKey(alias, password);
            System.out.println(secretKey.getEncoded());

            System.out.println(new BigInteger(1, secretKey.getEncoded()).toString(16));

            // create new key
//SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
// get base64 encoded version of the key
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            System.out.println("encodedKey.B64: " + encodedKey);

            //
            byte[] encodedKey2 = com.asia.token.Base64url.decode(encodedKey);
            SecretKey originalKey = new SecretKeySpec(encodedKey2, 0, encodedKey2.length, "AES");

            System.out.println("original key: " + originalKey);
            System.out.println("encodedKey2.lenght: " + encodedKey2.length);
            System.out.println("encodedKey2.bytes[]: ");
            for (int i = 0; i < encodedKey2.length; i++) {
                System.out.print(encodedKey2[i] + "|");

            }
            System.out.println();

        }
    }
}
