package com.cisco.cmad;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.shiro.codec.Base64;

import io.netty.handler.codec.http.HttpResponseStatus;
import de.flapdoodle.embed.mongo.MongodProcess;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

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

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;


public class MyBlogSpotVerticle extends AbstractVerticle{
	
	private static MongoClient client;
	protected AuthProvider authProvider;
	private static final Charset UTF8 = Charset.forName("UTF-8");
//    static Datastore dbstore ;
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
		router.post("/Services/rest/blogs").handler(this::handleNewBlog);
		router.post("/Services/rest/blogs/:blogId/comments").handler(this::handleNewComment);
		router.post("/Services/rest/blogs?tag=:tags").handler(this::handleSearchBlogs);
		router.get("/Services/rest/company").handler(this::handleGetCompanies);
		router.post("/Services/rest/user/auth").handler(this::handleLogin);
		 configureMongoClient();
		router.route().handler(StaticHandler.create().setCachingEnabled(false)::handle);
		HttpServerOptions serverOptions = new HttpServerOptions().setSsl(true);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);	
		
		//configureMongoClient();
		
		System.out.println("MyBlogSpot verticle started");
		startFuture.complete();
	}

	private void configureMongoClient() {
		client = MongoClient.createShared(vertx, new JsonObject().put("db_name", "blog_db"));
		JsonObject authProperties = new JsonObject();
//		MongoAuth authProvider = MongoAuth.create(client, authProperties);
//	   authProvider.getHashStrategy().setSaltStyle(HashSaltStyle.NO_SALT);;
//	   authProvider.setCollectionName("user");
//	   authProvider.setPasswordCredentialField("Password");
//	   authProvider.setUsernameCredentialField("userName");
	    
	}
	
//	public JsonObject createAuthInfo(String username, String password) {
//	    JsonObject authInfo = new JsonObject();
//	    authInfo.put(authProvider.getUsernameField(), username).put(authProvider.getPasswordField(), password);
//	    return authInfo;
//	  }
	
	@Override
	public void stop(Future<Void> stopFuture){
		System.out.println("MyRESTVerticle stopped");
		stopFuture.complete();
	}
	
	private void shiroUpdate(String userName,String Password){
	      try {

	          FileWriter fw = new FileWriter("src/main/resources/vertx-users.properties",true); //the true will append the new data
	          fw.write("\nuser."+userName+" = "+Password+",administrator");//appends the string to the file
	          fw.close();
	          System.out.println("success"+"\n ");
	      }
	       catch (IOException e) {
	          System.out.println("exception occoured"+ e);
	       }
	}
	private void handleGetSitesOfCompany(RoutingContext routingContext) {	
		System.out.println("session user:" + routingContext.user().principal());


		client.findOne("company", new JsonObject().put("_id",routingContext.request().getParam("companyId") ),null, results -> {
			if (results.succeeded()) {
				Company company = new Company(results.result());
				int i;
				JsonArray resJson = new JsonArray();
				for (i=0;i<company.getSite().size();i++){
					resJson.add(
							new JsonObject()

							.put("id", company.getSite().get(i).getId().toString())
							.put("siteName", company.getSite().get(i).getSiteName())

							.put("companyId",routingContext.request().getParam("companyId"))
							.put("subdomain", company.getSite().get(i).getSubdomain())
						);
			
			}
				routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());
		}
			else {routingContext.response().setStatusCode(400).putHeader("content-type", "application/json").end();}
			
	});
		}
	
	private void handleGetDepartmentsOfSite(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		

		client.findOne("site", new JsonObject().put("_id",routingContext.request().getParam("siteId") ),null, results -> {

				if (results.succeeded()) {
					Site site = new Site(results.result());
					int i;
					JsonArray resJson = new JsonArray();
					for (i=0;i<site.getDept().size();i++){
						resJson.add(
								new JsonObject()

								.put("id", site.getDept().get(i).getId().toString())
								.put("deptName", site.getDept().get(i).getDeptName())
								.put("siteId",routingContext.request().getParam("siteId"))
							);
				
				}
					routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());
			}
				else{
					
					routingContext.response().setStatusCode(400).putHeader("content-type", "application/json").end();
				}
				
		});
		
	}
	
	private void handlePerformRegistration(RoutingContext routingContext) {
		
		boolean isCompany = routingContext.getBodyAsJson().getValue("isCompany").toString().equals("true");		
		 Dept ndept ;  
	if (isCompany){
		ndept = new Dept(routingContext.getBodyAsJson().getValue("deptName").toString());
		client.save("dept",ndept.toJson(),res->{
		if (res.succeeded()) {
			client.findOne("dept",new JsonObject().put("deptName",ndept.getDeptName()),null, responseDept ->{
				if (responseDept.succeeded()){
					Dept dept = new Dept(responseDept.result());
					Site nSite = new Site(routingContext.getBodyAsJson().getValue("companyName").toString(),routingContext.getBodyAsJson().getValue("subdomain").toString(),dept);
					client.save("site",nSite.toJson(),resultSite->{
					  if (resultSite.succeeded()) {
						  client.findOne("site",new JsonObject().put("subdomain",nSite.getSubdomain()),null, responseSite ->{
					    	if (responseSite.succeeded()){
							   Site site = new Site(responseSite.result());
							   System.out.println(site.getId());
							   Company	company = new Company(routingContext.getBodyAsJson().getValue("companyName").toString(),site);
							   client.save("company",company.toJson(),resultCompany->{
								if (resultCompany.succeeded()) {
									client.findOne("company",new JsonObject().put("companyName",company.getCompanyName()),null, responseCompany ->{
										if (responseCompany.succeeded()){
											Company companyres = new Company(responseCompany.result());
											BlogUsers newUser = new BlogUsers(routingContext.getBodyAsJson().getValue("userName").toString(),
																	routingContext.getBodyAsJson().getValue("password").toString(),
																	routingContext.getBodyAsJson().getValue("email").toString(),
																	routingContext.getBodyAsJson().getValue("first").toString(),
																	routingContext.getBodyAsJson().getValue("last").toString(),companyres,site,dept);
											client.save("user",newUser.toJson(),userSave->{
												if (userSave.succeeded()) {
													shiroUpdate(newUser.getUserName(),newUser.getPassword());
													routingContext.response()
													.setStatusCode(201)
													.putHeader("content-type", "application/json; charset=utf-8")
													.end(Json.encodePrettily("success"));
													
												} else {
													userSave.cause().printStackTrace();
													routingContext.response()
													.setStatusCode(400)
													.end(Json.encodePrettily("failed"));}});
										} else {
										    responseCompany.cause().printStackTrace();
										}
									});


								} else {
							    resultCompany.cause().printStackTrace();
								}
							   	});
					    	} else {
				    responseSite.cause().printStackTrace();
				    routingContext.response()
				    .setStatusCode(401)
				    .end(Json.encodePrettily("failed"));
					    	}
						  });			
					  }});
				}});
			}});		
		}
		else {
			client.findOne("company",new JsonObject().put("_id",routingContext.getBodyAsJson().getValue("companyId").toString() ),null, resultCompany ->{
				if (resultCompany.succeeded()){
				Company	companyres = new Company(resultCompany.result());
				client.findOne("site",new JsonObject().put("_id",routingContext.getBodyAsJson().getValue("siteId").toString()),null, resultSite ->{
					if (resultSite.succeeded()){
					Site site = new Site(resultSite.result());
					client.findOne("dept",new JsonObject().put("_id",routingContext.getBodyAsJson().getValue("deptId").toString()),null, resultDept ->{
						if (resultDept.succeeded()){
						Dept dept = new Dept(resultDept.result());
						BlogUsers newUser = new BlogUsers(routingContext.getBodyAsJson().getValue("userName").toString(),
								routingContext.getBodyAsJson().getValue("password").toString(),
								routingContext.getBodyAsJson().getValue("email").toString(),
								routingContext.getBodyAsJson().getValue("first").toString(),
								routingContext.getBodyAsJson().getValue("last").toString(),companyres,site,dept);
						client.save("user",newUser.toJson(),res->{
							  if (res.succeeded()) {
								  shiroUpdate(newUser.getUserName(),newUser.getPassword());
									 routingContext.response()
								      .setStatusCode(201)
								      .putHeader("content-type", "application/json; charset=utf-8")
								      .end(Json.encodePrettily("success"));
							  } else {
							    res.cause().printStackTrace();
							    routingContext.response()
							    .setStatusCode(400)
							    .end(Json.encodePrettily("failed"));
							  }
						});

						}
						else {
							resultDept.cause().printStackTrace();
						}});
						}
					
					else {
						resultSite.cause().printStackTrace();
					}});

				
				}
				else {
					resultCompany.cause().printStackTrace();
				}
			
			});

		}

		
	}

	

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
	

	private JsonArray resolveBlogEntry(ArrayList<BlogEntry> be){
		JsonArray resJson = new JsonArray();
		for (int i=0;i<be.size();i++){
		resJson.add(new JsonObject()
				.put("id", be.get(i).getId())
				.put("content",be.get(i).getContent())
				.put("tags",be.get(i).getTags().toString())
				.put("title",be.get(i).getTitle())
				.put("userFirst", be.get(i).getUser().getFirst())
				.put("userLast", be.get(i).getUser().getLast())
				.put("userId",be.get(i).getUser().getId().toString())
				.put("date",be.get(i).getDate())
				);
		}
		return resJson;
	}
	
	private void handleLoadBlogs(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		client.find("blogs", new JsonObject().put("type","blog") , results -> {
		
		if (results.succeeded()){	
			System.out.println(results.result()+"\n");
			List<JsonObject> objects = results.result();

		List<BlogEntry> blogs = objects.stream().map(BlogEntry::new).collect(Collectors.toList());
		JsonArray resJson = new JsonArray();
		int i;
		for (i=0;i<blogs.size();i++){
			resJson.add(
					new JsonObject()
					.put("id", blogs.get(i).getId())
					.put("content",blogs.get(i).getContent())
					.put("tags",blogs.get(i).getTags().toString())
					.put("title",blogs.get(i).getTitle())
					.put("userFirst", blogs.get(i).getUser().getFirst())
					.put("userLast", blogs.get(i).getUser().getLast())
					.put("userId",blogs.get(i).getUser().getId().toString())
					.put("date",blogs.get(i).getDate())
					.put("comments",resolveBlogEntry(blogs.get(i).getComments()))
					);
			
		}
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
		//routingContext.response().setStatusCode(401).end();
		}
		else {
			routingContext.response().setStatusCode(401).end();
		}
		
		}
				);
	}
	private void handleNewComment(RoutingContext routingContext) {
		String username =routingContext.user().principal().getString("username");
		
		client.findOne("blogs", new JsonObject().put("_id",routingContext.request().getParam("id")),null , results -> {
				if (results.succeeded()){
					BlogEntry blog = new BlogEntry(results.result());

		client.findOne("user", new JsonObject().put("username",username),null, res ->{
			if (res.succeeded()){
				BlogUsers bloguser = new BlogUsers(res.result());
				BlogEntry comment = new BlogEntry(routingContext.getBodyAsJson().getValue("content").toString(),
						routingContext.getBodyAsJson().getValue("tags").toString(),
						routingContext.getBodyAsJson().getValue("title").toString(),
						bloguser,"comment"
						);
				blog.addComment(comment);
				client.save("blogs",comment.toJson(),blogsave->{
					if (blogsave.succeeded()){
						routingContext.response().putHeader("content-type", "application/json").end("success");		
					}
					else {				routingContext.response().setStatusCode(401).end();}
				});
				client.save("blogs",blog.toJson(),blogsave->{
					if (blogsave.succeeded()){
						routingContext.response().putHeader("content-type", "application/json").end("success");		
					}
					else {	routingContext.response().setStatusCode(401).end();}
				});
				}
				else{
					routingContext.response().setStatusCode(401).end();
				}
			});

				}
				
		});
	
		
	}
	private void handleNewBlog(RoutingContext routingContext) {
		System.out.println("session user:" + routingContext.user().principal());
		String username = routingContext.user().principal().getString("username");
		client.findOne("user", new JsonObject().put("userName",username),null, res ->{
			if (res.succeeded()){
				System.out.println(res.result()+"\n"+username);
				BlogUsers bloguser = new BlogUsers(res.result());				
			BlogEntry blog = new BlogEntry(routingContext.getBodyAsJson().getValue("content").toString(),
					routingContext.getBodyAsJson().getValue("tags").toString(),
					routingContext.getBodyAsJson().getValue("title").toString(),
					bloguser,"blog"
					);			
			client.save("blogs",blog.toJson(),blogsave->{
				if (blogsave.succeeded()){
					routingContext.response().putHeader("content-type", "application/json").end("success");		
				}
				else {				routingContext.response().setStatusCode(401).end();}
			});

			}
			else{
				routingContext.response().setStatusCode(401).end();
			}
		});

		
		
	}
	
	private void handleGetCompanies(RoutingContext routingContext) {
		//System.out.println("session user:" + routingContext.user().principal());
		JsonArray resJson = new JsonArray();
		client.find("company", new JsonObject() , results -> {
			if (results.succeeded()){
				int i;
				 List<JsonObject> objects = results.result();
			     List<Company> list = objects.stream().map(Company::new).collect(Collectors.toList());
			    
			     for(i = 0; i<list.size();i++){
			    	 resJson.add(
			    			 new JsonObject()
			    			 .put("id",list.get(i).getId())
			    			 .put("companyName",list.get(i).getCompanyName())
			    			 .put("subdomain",list.get(i).getSite().get(list.get(i).getSite().size()-1).getSubdomain())
			    			 );
			    	 
			     }
			     routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());	
			}
			if (results.failed()){
				System.out.println("Failed to get Companies");
				routingContext.response().setStatusCode(400)
				.putHeader("content-type", "application/json").end(resJson.encode());	
			}
		
		
		});

	
	}
	
	private void handleSearchBlogs(RoutingContext routingContext){
		String tag = routingContext.request().getParam("tags");
		System.out.println("asd\n"+tag);
		client.find("blogs", new JsonObject().put("type","blog") , results -> {
		
		if (results.succeeded()){	
			System.out.println(results.result()+"\n");
			List<JsonObject> objects = results.result();

		List<BlogEntry> blogs = objects.stream().map(BlogEntry::new).collect(Collectors.toList());
		JsonArray resJson = new JsonArray();
		int i;
		for (i=0;i<blogs.size();i++){
			resJson.add(
					new JsonObject()
					.put("id", blogs.get(i).getId())
					.put("content",blogs.get(i).getContent())
					.put("tags",blogs.get(i).getTags().toString())
					.put("title",blogs.get(i).getTitle())
					.put("userFirst", blogs.get(i).getUser().getFirst())
					.put("userLast", blogs.get(i).getUser().getLast())
					.put("userId",blogs.get(i).getUser().getId().toString())
					.put("date",blogs.get(i).getDate())
					.put("comments",resolveBlogEntry(blogs.get(i).getComments()))
					);
			
		}
		routingContext.response().putHeader("content-type", "application/json").end(resJson.encode());		
		//routingContext.response().setStatusCode(401).end();
		}
		else {
			routingContext.response().setStatusCode(401).end();
		}
		
		}
				);
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
	    	  User user = res.result();
	    	  JsonObject resjson = new JsonObject().put("username",user.principal().getString("username"));
	    		  
	  
//	    	  routingContext.setUser(user);
	    	  routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").setStatusCode(HttpResponseStatus.CREATED.code()).end(resjson.encodePrettily());
	    	  
	      } else {
	    	  routingContext.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
	    	  System.out.println("Incorrect username or password");
	      }
	    });
	}
}

