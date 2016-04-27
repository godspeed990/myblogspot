package com.cisco.cmad;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MyBlogSpot extends AbstractVerticle{
	
	public static void main(String args[]){
		
		//Get hold of vertx
		VertxOptions options = new VertxOptions().setWorkerPoolSize(10);
		DeploymentOptions depOptions = new DeploymentOptions();
		Vertx vertx = Vertx.vertx(options);
				
		//Deploy this verticle
		vertx.deployVerticle(MyBlogSpot.class.getName(), depOptions);
	}
	
	@Override
	public void start(Future<Void> startFuture){
		
		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		router.get("/Services/rest/company/:companyId/sites").handler(this::handleGetSitesOfCompany);
		router.get("/Services/rest/company/:companyId/sites/:siteId/departments").handler(this::handleGetDepartmentsOfSite);
		router.post("/Services/rest/user/register").handler(this::handlePerformRegistration);
		router.get("/Services/rest/user").handler(this::handleLoadSignedInUser);
		router.get("/Services/rest/blogs").handler(this::handleLoadBlogs);
		router.post("/Services/rest/blogs/:blogId/comments").handler(this::handleNewBlog);
		router.get("/Services/rest/company").handler(this::handleGetCompanies);
		router.post("/Services/rest/user/auth").handler(this::handleLogin);
		
		router.route().handler(StaticHandler.create()::handle);
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);	
		System.out.println("MyBlogSpot verticle started");
		startFuture.complete();
	}
	
	@Override
	public void stop(Future<Void> stopFuture){
		System.out.println("MyRESTVerticle stopped");
		stopFuture.complete();
	}
	
	private void handleGetSitesOfCompany(RoutingContext routingContext) {	
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
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());
	}
	
	private void handleLoadSignedInUser(RoutingContext routingContext) {
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
			);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
	}
	
	private void handleNewBlog(RoutingContext routingContext) {
		routingContext.response().end();
	}
	
	private void handleGetCompanies(RoutingContext routingContext) {
		JsonArray resJson = new JsonArray().add(
					new JsonObject().put("id", "55716669eec5ca2b6ddf5626").put("companyName", "Acme Inc").put("subdomain", "acme")
				).add(
						new JsonObject().put("id", "559e4331c203b4638a00ba1a").put("companyName", "Acme Inc").put("subdomain", "acme")
		);
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
	}
	
	private void handleLogin(RoutingContext routingContext) {
		routingContext.response().end();
	}
}

