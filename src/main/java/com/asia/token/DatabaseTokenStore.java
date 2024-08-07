package com.asia.token;

///import com.asia.token.TokenStore.Token;
import org.dalesbred.Database;
import org.json.JSONObject;
import spark.Request;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Optional;

public class DatabaseTokenStore implements TokenStore {

    private final Database database;
    private final SecureRandom secureRandom;

    public DatabaseTokenStore(Database database) {
        this.database = database;
        this.secureRandom = new SecureRandom();
    }

    private String randomId() {
        System.out.println(">>>DatabaseTokenStore.randomId() <<<");
        var bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        System.out.println(">>>DatabaseToken.randomId().20_bytes[]___random: ");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i] + "|");
        }
        System.out.println();
        byte bytesHardCode[] = {-55, 91, -54, 5, 11, -57, 22, -17, -116, -48, 91, 16, -35, 68, 56, 97, 11, 125, -89, 68};
        System.out.println(">>>DatabaseTokenStore.randomId()bytesHardCode.length: " + bytesHardCode.length);
        System.out.println(">>>DatabaseTokenStore.randomId().Base64url.encode(bytes): " + Base64url.encode(bytes));
        return Base64url.encode(bytes);
    }

    @Override
    public String create(Request request, Token token) {
        System.out.println(">>>DatabaseTokenStore.create() <<<");
        var tokenId = randomId();
        var attrs = new JSONObject(token.attributes).toString();
        database.updateUnique("INSERT INTO tokens(token_id, user_id, expiry, attributes) VALUES(?, ?, ?, ?)", tokenId, token.username, token.expiry, attrs);

        return tokenId;
    }

    @Override
    public Optional<Token> read(Request request, String tokenId) {
        System.out.println(">>> INI: DatabaseTokenStore.READ(" + request.toString() + ", " + tokenId + ") <<<");
        return database.findOptional(rs -> this.readToken(rs), "SELECT user_id, expiry, attributes "
                + "FROM tokens WHERE token_id = ?", tokenId);

        /*Optional<ResultSet> rs;
        rs = database.findOptional(Token,"SELECT user_id, expiry, attributes    FROM tokens WHERE token_id = ?", tokenId);

        Token tkn = readToken(rs);
        return tkn;*/
    }

    private Token readToken(ResultSet resultSet) throws SQLException {
        System.out.println(">>> INI: DatabaseTokenStore.READTOKEN(" + resultSet.toString() + ") <<<");
        var username = resultSet.getString(1);
        var expiry = resultSet.getTimestamp(2).toInstant();
        var json = new JSONObject(resultSet.getString(3));
        var token = new Token(expiry, username);
        for (var key : json.keySet()) {
            token.attributes.put(key, json.getString(key));
        }
        return token;
    }

    @Override
    public void revoke(Request request, String tokenId) {
        database.update("DELETE FROM tokens WHERE token_id = ?", tokenId);
    }
}
