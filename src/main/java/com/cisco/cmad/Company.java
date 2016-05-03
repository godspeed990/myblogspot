package com.cisco.cmad;

import java.util.ArrayList;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(value="company")
public class Company {
	@Id
	private String id;
	private String companyName;
	@Embedded
	private ArrayList<Site> site= new ArrayList<Site>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public ArrayList<Site> getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site.add(site);
	}
	
	public JsonArray sitetoJsonArray(){
		JsonArray jArray = new JsonArray();
		for (int i =0;i<site.size();i++){
			jArray.add(site.get(i).toJson());
		}
		return jArray;
	}
	
	public JsonObject toJson() {
		    JsonObject json = new JsonObject();
		    if (id != null && !id.isEmpty()) {
					      json.put("_id", id);
					    }
		    	json.put("companyName", this.companyName)
		        .put("site", sitetoJsonArray());
		    
		    return json;
	}
		
	public Company(String companyName) {
		super();
		this.companyName = companyName;
		
	}
	public Company(String companyName, Site site) {
		super();
		this.companyName = companyName;
		this.site.add(site);
	}
	public Company(){}
	
	public Company(JsonObject obj){
		this.id = obj.getString("_id");
		this.companyName =obj.getString("companyName");
		JsonArray arr = obj.getJsonArray("site");
		for (int i =0;i<arr.size();i++){
		this.site.add(new Site(arr.getJsonObject(i)));}
	}

}
