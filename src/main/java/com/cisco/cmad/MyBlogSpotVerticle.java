package com.cisco.cmad;

import java.nio.charset.Charset;

import org.apache.shiro.codec.Base64;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class MyBlogSpotVerticle extends AbstractVerticle{
	
	private AuthProvider authProvider;
	private MongoClient client;
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	public static void main(String args[]){
		
		//Get hold of vertx
		VertxOptions options = new VertxOptions().setWorkerPoolSize(10);
		DeploymentOptions depOptions = new DeploymentOptions();
		Vertx vertx = Vertx.vertx(options);
				
		//Deploy this verticle
		vertx.deployVerticle(MyBlogSpotVerticle.class.getName(), depOptions);
	}
	
	@Override
	public void start(Future<Void> startFuture){
		
		Router router = Router.router(vertx);
		
		// We need cookies, sessions and request bodies	    
	    router.route().handler(CookieHandler.create());
	    router.route().handler(BodyHandler.create());
	    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

	    // Simple auth service which uses a properties file for user/role info
	    authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, new JsonObject());

	    // We need a user session handler too to make sure the user is stored in the session between requests
	    router.route().handler(UserSessionHandler.create(authProvider));	
		
	    AuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
	    router.route("/Services/rest/company/*").handler(basicAuthHandler);
	    router.route("/Services/rest/user").handler(basicAuthHandler);
	    router.route("/Services/rest/blogs").handler(basicAuthHandler);
	    router.route("/Services/rest/user/auth").handler(basicAuthHandler);
		router.get("/Services/rest/company/:companyId/sites").handler(this::handleGetSitesOfCompany);
		router.get("/Services/rest/company/:companyId/sites/:siteId/departments").handler(this::handleGetDepartmentsOfSite);
		router.post("/Services/rest/user/register").handler(this::handlePerformRegistration);
		router.get("/Services/rest/user").handler(this::handleLoadSignedInUser);
		router.get("/Services/rest/blogs").handler(this::handleLoadBlogs);
		router.post("/Services/rest/blogs/:blogId/comments").handler(this::handleNewBlog);
		router.get("/Services/rest/company").handler(this::handleGetCompanies);
		router.post("/Services/rest/user/auth").handler(this::handleLogin);
		
		router.route().handler(StaticHandler.create()::handle);
		HttpServerOptions serverOptions = new HttpServerOptions().setSsl(true);
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);	
		
		//configureMongoClient();
		
		System.out.println("MyBlogSpot verticle started");
		startFuture.complete();
	}

	private void configureMongoClient() {
		client = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
	    JsonObject authProperties = new JsonObject();
	    authProvider = MongoAuth.create(client, authProperties);
	}
	
	@Override
	public void stop(Future<Void> stopFuture){
		System.out.println("MyRESTVerticle stopped");
		stopFuture.complete();
	}
	
	private void handleGetSitesOfCompany(RoutingContext routingContext) {	
		System.out.println("session user:" + routingContext.user().principal());
		JsonArray resJson = new JsonArray().add(
				new JsonObject()
				.put("id", "55716669eec5ca2b6ddf5627")
				.put("siteName", "Acme Inc")
				.put("companyId", "55716669eec5ca2b6ddf5626")
				.put("subdomain", "acme")
			);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());
	}
	
	private void handleGetDepartmentsOfSite(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		JsonArray resJson = new JsonArray().add(
				new JsonObject()
				.put("id", "55716669eec5ca2b6ddf5628")
				.put("deptName", "Sales")
				.put("siteId", "55716669eec5ca2b6ddf5627")
			);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());
	}
	
	private void handlePerformRegistration(RoutingContext routingContext) {
		JsonObject resJson = new JsonObject()
				.put("userName", "Vinay")
				.put("password", "abc123")
				.put("email", "vinay@gmail.com")
				.put("first", "Vinay")
				.put("last", "Prasad")
				.put("companyId", "55716669eec5ca2b6ddf5626")
				.put("siteId", "55716669eec5ca2b6ddf5627")
				.put("deptId", "55716669eec5ca2b6ddf5628");
		routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").setStatusCode(HttpResponseStatus.CREATED.code()).end(resJson.encode());
	}
	
	/*
	 * Commenting for now since there is no need to do authentication ourselves on each call
	 * private void handlePerformRegistration(RoutingContext routingContext) {
		String authorizationHeader = routingContext.request().getHeader(HttpHeaders.AUTHORIZATION);
		JsonObject authInfo = getAuthInfoFromAuthHeader(routingContext, authorizationHeader);

	    authProvider.authenticate(authInfo, res -> {
	      if (res.succeeded()) {
	    	  System.out.println("Success");
	    	  JsonObject resJson = new JsonObject()
	    				.put("userName", "Vinay")
	    				.put("password", "abc123")
	    				.put("email", "vinay@gmail.com")
	    				.put("first", "Vinay")
	    				.put("last", "Prasad")
	    				.put("companyId", "55716669eec5ca2b6ddf5626")
	    				.put("siteId", "55716669eec5ca2b6ddf5627")
	    				.put("deptId", "55716669eec5ca2b6ddf5628");
	    	  routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").setStatusCode(HttpResponseStatus.CREATED.code()).end(resJson.encode());
	      } else {
	    	  routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
	    	  System.out.println("Incorrect username or password");
	      }
	    });
	}*/

	//The auth info is not set in the Auth header in the current UI code. This is not needed hence
	private JsonObject getAuthInfoFromAuthHeader(RoutingContext routingContext, String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Basic")) {
            routingContext.fail(HttpResponseStatus.BAD_REQUEST.code());
        } 
        
        String base64Credentials = authorizationHeader.substring("Basic".length()).trim();
        //check if we have valid base64encoded data set
        if (base64Credentials == null || base64Credentials.isEmpty()) {
        	routingContext.fail(HttpResponseStatus.BAD_REQUEST.code());
        } 
        
        //decode the credentials
        String credentials = new String(Base64.decode(base64Credentials), UTF8);
        // credentials = username:password
        final String[] values = credentials.split(":", 2);
        
        //ideally we expect username and password alone be percent after split in position 0 and 1 respectively.
        if(values == null || values.length != 2){
        	routingContext.fail(HttpResponseStatus.BAD_REQUEST.code());
        }

		JsonObject authInfo = new JsonObject().put("username", values[0]).put("password", values[1]);
		return authInfo;
	}
	
	private void handleLoadSignedInUser(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		JsonObject resJson = new JsonObject()
				.put("id", "55716669eec5ca2b6ddf5629")
				.put("password", "abc123")
				.putNull("companyId")
				.putNull("deptId")
				.putNull("isCompany")
				.putNull("companyName")
				.putNull("subdomain")
				.putNull("deptName")
				.put("email", "maruthiabc.com")
				.put("username", "maruthirj")
				.put("last", "RJ")
				.put("first", "Maruthi")
				.put("last", "Prasad")
				.put("siteId", "55716669eec5ca2b6ddf5627");
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
	}
	
	private void handleLoadBlogs(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		//authProvider.authenticate(, resultHandler);
		JsonArray resJson = new JsonArray().add(
				new JsonObject()
				.put("id", "55716669eec5ca2b6ddf5626")
				.put("content", "Blog 2 text here Blog text here...")
				.put("tags", "abc,lmn")
				.put("title", "blog2")
				.put("userFirst", "Maruthi")
				.put("userLast", "RJ")
				.put("userId", "55716669eec5ca2b6ddf5629")
				.put("date", "1438079496982")
				.put("comments", new JsonArray().add(
						new JsonObject()
						.put("content", "Nice blog")
						.putNull("blogId")
						.put("userFirst", "Maruthi")
						.put("userLast", "RJ")
						.put("userId", "55716669eec5ca2b6ddf5629")
						.put("date", "1438079510294")
						))
				
				)
				.add(
						new JsonObject()
						.put("id", "55716669eec5ca2b6ddf5626")
						.put("content", "Blog 2 text here Blog text here...")
						.put("tags", "abc,lmn")
						.put("title", "blog2")
						.put("userFirst", "Maruthi")
						.put("userLast", "RJ")
						.put("userId", "55716669eec5ca2b6ddf5629")
						.put("date", "1438079496982")
						.put("comments", new JsonArray().add(
								new JsonObject()
								.put("content", "Nice blog")
								.putNull("blogId")
								.put("userFirst", "Maruthi")
								.put("userLast", "RJ")
								.put("userId", "55716669eec5ca2b6ddf5629")
								.put("date", "1438079510294")
								))
						);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
		//routingContext.response().setStatusCode(401).end();
	}
	
	private void handleNewBlog(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		routingContext.response().end();
	}
	
	private void handleGetCompanies(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		JsonArray resJson = new JsonArray().add(
					new JsonObject().put("id", "55716669eec5ca2b6ddf5626").put("companyName", "Acme Inc").put("subdomain", "acme")
				).add(
						new JsonObject().put("id", "559e4331c203b4638a00ba1a").put("companyName", "Acme Inc").put("subdomain", "acme")
		);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
	}
	
	//Will not work since the current UI code is sending the username and password in the POST payload and not in the Auth header
	private void handleLogin(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		
		JsonObject reqPayload = new JsonObject(routingContext.getBodyAsString());
		if ( reqPayload.containsKey("userName") == false || reqPayload.containsKey("password") == false) {
			 routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
	    	  System.out.println("Username password not specifed.");
		}
		JsonObject authInfo = new JsonObject()
								.put("username", reqPayload.getString("userName"))
								.put("password", reqPayload.getString("password"));

	    authProvider.authenticate(authInfo, res -> {
	      if (res.succeeded()) {
	    	  System.out.println("Success");
	    	  JsonObject resJson = new JsonObject()
	    				.put("userName", "Vinay")
	    				.put("password", "abc123")
	    				.put("email", "vinay@gmail.com")
	    				.put("first", "Vinay")
	    				.put("last", "Prasad")
	    				.put("companyId", "55716669eec5ca2b6ddf5626")
	    				.put("siteId", "55716669eec5ca2b6ddf5627")
	    				.put("deptId", "55716669eec5ca2b6ddf5628");
	    	  routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").setStatusCode(HttpResponseStatus.CREATED.code()).end(resJson.encode());
	      } else {
	    	  routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
	    	  System.out.println("Incorrect username or password");
	      }
	    });
	}
}

