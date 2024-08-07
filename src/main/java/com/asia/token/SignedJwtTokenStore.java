package com.asia.token;

import java.util.Date;
import java.util.Optional;

import java.text.ParseException;
import javax.crypto.SecretKey;

import spark.Request;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

public class SignedJwtTokenStore implements TokenStore {

    private final JWSSigner signer;
    private final JWSVerifier verifier;
    private final JWSAlgorithm algorithm;
    private final String audience;

    public SignedJwtTokenStore(JWSSigner signer, JWSVerifier verifier, JWSAlgorithm algorithm, String audience) {
        this.signer = signer;
        this.verifier = verifier;
        this.algorithm = algorithm;
        this.audience = audience;
    }

    @Override
    public String create(Request request, Token token) {
        var claimsSet = new JWTClaimsSet.Builder()
                .subject(token.username)
                .audience(audience)
                .expirationTime(Date.from(token.expiry))
                .claim("attrs", token.attributes)
                .build();

        var header = new JWSHeader(JWSAlgorithm.HS256);
        System.out.println(">>>SignedJwtTokenStore.create().header: " + header.toString());
        var jwt = new SignedJWT(header, claimsSet);
        System.out.println(">>>SignetJwtTokenStore.create().jwt.BEFORE_SIGN.getPayload: " + jwt.getPayload());

        PlainHeader pHeader = new PlainHeader.Builder().
                contentType("text/plain")
                .customParam("enterprise", "fpvSoft")
                .customParam("exp", new Date().getTime())
                .build();

        PlainJWT plainJWT = new PlainJWT(pHeader, claimsSet);
        System.out.println(">>>SignedJwtTokenStore.create().PLAIN_JWT: " + plainJWT.serialize());
        try {
            jwt.sign(signer);
            System.out.println(">>>SignedJwtTokenStore.create().jwt.sign().serialize: " + jwt.serialize());
            System.out.println();
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

     @Override
    public Optional<Token> read(Request request, String tokenId) {
        try {
            var jwt = SignedJWT.parse(tokenId);
            if (!jwt.verify(verifier)) {
                throw new JOSEException("Invalid signature");
            }
            var claims = jwt.getJWTClaimsSet();
            if (!claims.getAudience().contains(audience)) {
                throw new JOSEException("Incorrect audience");
            }
            var expiry = claims.getExpirationTime().toInstant();
            var subject = claims.getSubject();
            var token = new Token(expiry, subject);
            var attrs = claims.getJSONObjectClaim("attrs");
            attrs.forEach((key, value) -> token.attributes.put(key, (String) value));
            return Optional.of(token);
        } catch (ParseException | JOSEException e) {
            return Optional.empty();
        }
    }
    

    @Override
    public void revoke(Request request, String tokenId) {
        // TODO
    }

}
