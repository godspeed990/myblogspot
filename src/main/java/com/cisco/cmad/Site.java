package com.cisco.cmad;

import java.util.ArrayList;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity (value = "site")
public class Site {
	@Id
	private String id;
	private String subdomain;
	private String siteName;
	@Embedded
	private ArrayList<Dept> dept= new ArrayList<Dept>();
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSubdomain() {
		return subdomain;
	}
	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}
	public ArrayList<Dept> getDept() {
		return dept;
	}
	public void setDept(ArrayList<Dept> dept) {
		this.dept = dept;
	}
	
	public void addDept(Dept dept) {
		this.dept.add(dept);
	}
	
	public JsonArray depttoJsonArray(){
		JsonArray jArray = new JsonArray();
		for (int i =0;i<dept.size();i++){
			jArray.add(dept.get(i).toJson());
		}
		return jArray;
	}
	
	public JsonObject toJson() {
	    JsonObject json = new JsonObject();
	    if (id != null && !id.isEmpty()) {
				      json.put("_id", id);
				    }
	    	json.put("siteName", this.siteName)
	        .put("subdomain", this.subdomain)
	        .put("dept",depttoJsonArray())
	        ;
	    
	    return json;
	}
	public Site(){}
	public Site( String siteName,String subdomain,Dept dept) {
		super();
		this.subdomain = subdomain;
		this.dept.add(dept);
		this.siteName = siteName;
	}
	
	public Site(JsonObject js){
		this.subdomain = js.getString("subdomain");
		this.siteName = js.getString("siteName");
		this.id = js.getString("_id");
		JsonArray arr = js.getJsonArray("dept");
		for (int i =0;i<arr.size();i++){
		this.dept.add(new Dept(arr.getJsonObject(i)));}
	}
	
}
