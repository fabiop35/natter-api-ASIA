package com.asia;

import org.dalesbred.Database;
import org.dalesbred.result.EmptyResultException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.json.*;

import java.nio.file.*;

import spark.*;
import static spark.Spark.*;

import com.google.common.util.concurrent.*;

import com.asia.controller.*;

public class Main {

  public static void main(String... args) throws Exception {
    var datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter", "password");
    var database = Database.forDataSource(datasource);
    createTables(database);
    datasource = JdbcConnectionPool.create("jdbc:h2:mem:natter", "natter_api_user", "password");
    database = Database.forDataSource(datasource);

    var spaceController = new SpaceController(database);
    post("/spaces", spaceController::createSpace);
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

    internalServerError(new JSONObject()
    .put("error", "internal server error").toString());
    notFound(new JSONObject()
     .put("error", "not found").toString());

    exception(IllegalArgumentException.class,Main::badRequest);
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
    database.update(Files.readString(path));          }

  private static void badRequest(Exception ex,
    Request request, Response response) {
    response.status(400);
    response.body(new JSONObject()
        .put("error", ex.getMessage()).toString());
  }

}
