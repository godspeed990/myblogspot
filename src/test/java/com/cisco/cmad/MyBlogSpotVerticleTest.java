package com.cisco.cmad;

import java.util.List;

import org.apache.shiro.codec.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MyBlogSpotVerticleTest {

    Vertx vertx;
    String name;
    public static final int PORT=8080;
    public static final String HOST="localhost";
    public static final String USERNAME="admin";
    public static final String PASSWORD="admin";

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MyBlogSpotVerticle(), context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
    
    @Test
    public void checkThatTheIndexPageIsServed(TestContext context) {
      Async async = context.async();
      vertx.createHttpClient().getNow(PORT, HOST, "/index.html", response -> {
        context.assertEquals(response.statusCode(), 200);
        context.assertEquals(response.headers().get("content-type"), "text/html");
        response.bodyHandler(body -> {
          context.assertTrue(body.toString().contains("MySocial"));
          async.complete();
        });
      });
    }

    @Test
    public void checkSuccessfulUserRegistration(TestContext context) {
        // Send a request and get a response
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        
        JsonObject resJson = new JsonObject()
			.put("userName", "Vinay")
			.put("password", "abc123")
			.put("email", "vinay@gmail.com")
			.put("first", "Vinay")
			.put("last", "Prasad")
			.put("companyId", "55716669eec5ca2b6ddf5626")
			.put("siteId", "55716669eec5ca2b6ddf5627")
			.put("deptId", "55716669eec5ca2b6ddf5628");

        client.post(PORT, HOST, "/Services/rest/user/register", resp -> {
        	context.assertEquals(resp.statusCode(), HttpResponseStatus.CREATED.code());
            resp.bodyHandler(body -> 
	            {
	            	context.assertEquals(resJson, new JsonObject(body.toString()));            	
	            	List<String> setCookies = resp.cookies();
	            	context.assertTrue(setCookies.size() != 0);
	            	context.assertNotNull(setCookies.get(0));
	            	client.close();
	                async.complete();
	            }
            );       
        })
        .putHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeToString(new String(USERNAME+ ":" + PASSWORD).getBytes()))
		.putHeader(HttpHeaders.CONTENT_LENGTH, "0").end();
    }
    
    @Test
    public void checkFailedUserLogin(TestContext context) {
        // Send a request and get a response
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        
        JsonObject resJson = new JsonObject()
			.put("userName", "Vinay")
			.put("password", "abc123")
			.put("email", "vinay@gmail.com")
			.put("first", "Vinay")
			.put("last", "Prasad")
			.put("companyId", "55716669eec5ca2b6ddf5626")
			.put("siteId", "55716669eec5ca2b6ddf5627")
			.put("deptId", "55716669eec5ca2b6ddf5628");

        client.post(PORT, HOST, "/Services/rest/user/auth", resp -> {
        	context.assertEquals(resp.statusCode(), HttpResponseStatus.UNAUTHORIZED.code());
            resp.bodyHandler(body -> 
	            {
	            	
	            	client.close();
	                async.complete();
	            }
            );
            
            
        })
        .putHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeToString(new String("admin1"+ ":" + PASSWORD).getBytes()))
		.putHeader(HttpHeaders.CONTENT_LENGTH, "0").end();;
    }
    
    @Test
    public void checkGetSitesOfCompany(TestContext context) {
        // Send a request and get a response
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        client.get(PORT, HOST, "/Services/rest/company/1/sites", resp -> {
        	context.assertEquals(resp.statusCode(), HttpResponseStatus.OK.code());
            resp.bodyHandler(body -> 
            {
            	//context.assertEquals(new JsonObject().put("id", "1").put("name", "Test User 1"), new JsonObject(body.toString()));
            	client.close();
                async.complete();
            }
            );
            
        })
        .putHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeToString(new String(USERNAME + ":" + PASSWORD).getBytes()))
		.putHeader(HttpHeaders.CONTENT_LENGTH, "0").end();
    }
    
    //Verify performRegistration does not require authentication
    @Test
    public void checkPerformRegistration(TestContext context) {
        // Send a request and get a response
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();

        client.post(PORT, HOST, "/Services/rest/user/register", resp -> {
        	context.assertEquals(resp.statusCode(), HttpResponseStatus.CREATED.code());
            resp.bodyHandler(body -> 
            {
            	//context.assertEquals(new JsonObject().put("id", "1").put("name", "Test User 1"), new JsonObject(body.toString()));
            	client.close();
                async.complete();
            }
            );
            
        })
        .putHeader(HttpHeaders.CONTENT_LENGTH, "0").end();
    }
}
