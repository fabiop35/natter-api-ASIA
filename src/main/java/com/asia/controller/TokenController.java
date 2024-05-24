package com.asia.controller;

import java.time.temporal.ChronoUnit;

import org.json.JSONObject;
import com.asia.token.TokenStore;
import com.asia.token.TokenStore.Token;
import spark.*;

import static java.time.Instant.now;
import java.util.Optional;

public class TokenController {

    private final TokenStore tokenStore;

    public TokenController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public JSONObject login(Request request, Response response) {
        System.out.println("<<< TokenController.login >>>");
        String subject = request.attribute("subject");
        var expiry = now().plus(10, ChronoUnit.MINUTES);
        var token = new TokenStore.Token(expiry, subject);
        var tokenId = tokenStore.create(request, token);
        System.out.println("===>TokenController.login.tokenId: "+tokenId);
        response.status(201);
        return new JSONObject()
                .put("token", tokenId);
    }

    public void validateToken(Request request, Response response) {
        System.out.println("INI: TokenController.validateToken");
        // WARNING: CSRF attack possible
        /*Optional<Token> tkn = tokenStore.read(request, null);
        if(tkn.isPresent()){
           Token token = tkn.get();
           if (now().isBefore(token.expiry)) {
                System.out.println("Token.username: "+token.username);
                request.attribute("subject", token.username);
                token.attributes.forEach(request::attribute);
            }
        } */

      var tokenId = request.headers("X-CSRF-Token");
      if (tokenId == null) return;


       tokenStore.read(request, tokenId).ifPresent((Token token) -> {
            if (now().isBefore(token.expiry)) {
                System.out.println("Token.username: "+token.username);
                request.attribute("subject", token.username);
                token.attributes.forEach(request::attribute);
            }
        });
    }

  public JSONObject logout(Request request, Response response) {
    
    var tokenId = request.headers("X-CSRF-Token");
    if (tokenId == null)
        throw new IllegalArgumentException("missing token header");
    tokenStore.revoke(request, tokenId);
    response.status(200);

    return new JSONObject();
  }
}
