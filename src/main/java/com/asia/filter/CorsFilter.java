package com.asia.filter;

import spark.*;
import java.util.*;
import static spark.Spark.*;
 
public class CorsFilter implements Filter {
  private final Set<String> allowedOrigins;
 
  public CorsFilter(Set<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }
 
  @Override
  public void handle(Request request, Response response) {
    System.out.println(">>> CORS.Filter.INI <<<");
    var origin = request.headers("Origin");
    System.out.println("origin: "+origin);
    origin = "http://localhost:9999";
    System.out.println("origin: "+origin);
    if (origin != null && allowedOrigins.contains(origin)) {
        System.out.println(">>> allowedOrigin");
        response.header("Access-Control-Allow-Origin", origin);
        response.header("Access-Control-Allow-Credentials", "true");
        response.header("Vary", "Origin");
    }
 
    if (isPreflightRequest(request)) {
      if (origin == null || !allowedOrigins.contains(origin)) {
          halt(403);
      }
      response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-CSRF-Token");
      response.header("Access-Control-Allow-Methods", "GET, POST, DELETE");
      halt(204);
    }
  }
 
  private boolean isPreflightRequest(Request request) {
      return "OPTIONS".equals(request.requestMethod()) && request.headers().contains("Access-Control-Request-Method");
  }
}

