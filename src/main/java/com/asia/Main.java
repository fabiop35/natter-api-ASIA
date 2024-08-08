package com.asia;

import org.dalesbred.Database;
import org.dalesbred.result.EmptyResultException;

import org.h2.jdbcx.JdbcConnectionPool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.SecretKey;

import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.secure;

import com.google.common.util.concurrent.RateLimiter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import software.pando.crypto.nacl.SecretBox;

import com.asia.controller.AuditController;
import com.asia.controller.ModeratorController;
import com.asia.controller.SpaceController;
import com.asia.controller.UserController;
import com.asia.controller.TokenController;

import com.asia.token.TokenStore;
import com.asia.token.CookieTokenStore;
import com.asia.token.DatabaseTokenStore;
import com.asia.token.EncryptedTokenStore;
import com.asia.filter.CorsFilter;
import com.asia.token.HmacTokenStore;
import com.asia.token.JsonTokenStore;
import com.asia.token.SignedJwtTokenStore;
import com.asia.token.EncryptedJwtTokenStore;

public class Main {

    public static void main(String... args) throws Exception {
        //secure("localhost.p12", "changeit", null, null);
        port(args.length > 0 ? Integer.parseInt(args[0]) : spark.Service.SPARK_DEFAULT_PORT);
        Spark.staticFiles.location("/public");

        var datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter", "password");
        var database = Database.forDataSource(datasource);
        createTables(database);
        datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter_api_user", "password");
        database = Database.forDataSource(datasource);

        var spaceController = new SpaceController(database);
        //post("/spaces", spaceController::createSpace);
        var userController = new UserController(database);

        /* TOKEN-BASED AUTHENTICATION */
        //TokenStore tokenStore = new CookieTokenStore();
        /* Loading the HMAC key (5.12) */
        var keyPassword = System.getProperty("keystore.password", "changeit").toCharArray();
        var keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("keystore.p12"), keyPassword);
        var macKey = keyStore.getKey("hmac-key", keyPassword);
        var encKey = keyStore.getKey("aes-key", keyPassword);

        
        /* Generate a signed JWT 
        var algorithm = JWSAlgorithm.HS256;
        var signer = new MACSigner((SecretKey) macKey);
        var verifier = new MACVerifier((SecretKey) macKey); */
        var naclKey = SecretBox.key(encKey.getEncoded());

        //TokenStore tokenStore = new EncryptedTokenStore(new JsonTokenStore(), naclKey);
        TokenStore tokenStore = new EncryptedJwtTokenStore((SecretKey) encKey);
        //tokenStore = new HmacTokenStore(tokenStore, macKey);
        //TokenStore tokenStore = new HmacTokenStore(databaseTokenStore, macKey);
        // TokenStore tokenStore = new DatabaseTokenStore(database);
        var tokenController = new TokenController(tokenStore);
        before(userController::authenticate);
        //before(tokenController::validateToken);
        before((req, res) -> tokenController.validateToken(req, res));

        var auditController = new AuditController(database);
        before(auditController::auditRequestStart);
        afterAfter(auditController::auditRequestEnd);
        before("/sessions", userController::requireAuthentication);
        post("/sessions", tokenController::login);
        delete("/sessions", tokenController::logout);

        before(userController::authenticate);

        before(auditController::auditRequestStart);
        afterAfter(auditController::auditRequestEnd);
        System.out.println("XXX requireAuthentication.");
        before("/spaces", userController::requireAuthentication);
        System.out.println("XXX createSpace");
        post("/spaces", spaceController::createSpace);
        get("/spaces/:spaceId/messages/:msgId", spaceController::readMessage);
        //Access Control
        before("/spaces/:spaceId/messages", userController.requirePermission("POST", "w"));
        post("/spaces/:spaceId/messages", spaceController::postMessage);

        before("/spaces/:spaceId/messages/*",
                userController.requirePermission("GET", "r"));
        get("/spaces/:spaceId/messages/:msgId", spaceController::readMessage);

        before("/spaces/:spaceId/messages",
                userController.requirePermission("GET", "r"));

        get("/spaces/:spaceId/messages",
                spaceController::findMessages);

        get("/logs", auditController::readAuditLog);
        post("/users", userController::registerUser);
        before("/spaces/:spaceId/members",
                userController.requirePermission("POST", "rwd"));
        post("/spaces/:spaceId/members", spaceController::addMember);

        var moderatorController = new ModeratorController(database);
        before("/spaces/:spaceId/messages/*", userController.requirePermission("DELETE", "d"));
        delete("/spaces/:spaceId/messages/:msgId", moderatorController::deletePost);

        after((request, response) -> {
            response.type("application/json");
        });

        //Rate-limiter
        var rateLimiter = RateLimiter.create(2.0d);
        before((request, response) -> {
            if (!rateLimiter.tryAcquire()) {
                response.header("Retry-After", "2");
                halt(429);
            }
        });

        //CORS
        before(new CorsFilter(Set.of("http://localhost:9999")));

        internalServerError(new JSONObject()
                .put("error", "internal server error").toString());
        notFound(new JSONObject()
                .put("error", "not found").toString());

        exception(IllegalArgumentException.class, Main::badRequest);
        exception(JSONException.class, Main::badRequest);
        exception(EmptyResultException.class, (e, request, response) -> response.status(404));

        afterAfter((request, response) -> {
            response.header("X-XSS-Protection", "0");
        });

        before(((request, response) -> {
            if (request.requestMethod().equals("POST") && !"application/json".equals(request.contentType())) {
                halt(415, new JSONObject().put("error", "Only application/json supported").toString());
            }
        }));

        afterAfter((request, response) -> {
            //response.header("Strict-Transport-Security", "max-age=31536000");
            response.type("application/json;charset=utf-8");
            response.header("X-Content-Type-Options", "nosniff");
            response.header("X-Frame-Options", "DENY");
            response.header("X-XSS-Protection", "0");
            response.header("Cache-Control", "no-store");
            response.header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; sandbox");
            response.header("Server", "");
        });

        internalServerError(new JSONObject()
                .put("error", "internal server error").toString());
        notFound(new JSONObject()
                .put("error", "not found").toString());
        exception(IllegalArgumentException.class, Main::badRequest);
        exception(JSONException.class, Main::badRequest);

    }

    private static void createTables(Database database) throws Exception {
        var path = Paths.get(
                Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
    }

    private static void badRequest(Exception ex,
            Request request, Response response) {
        response.status(400);
        response.body(new JSONObject()
                .put("error", ex.getMessage()).toString());
    }

}
