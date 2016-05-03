package com.cisco.cmad;



import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import io.vertx.core.json.JsonObject;

@Entity(value = "dept")
public class Dept {
	@Id
	private String id;
	private String deptName;

	public Dept(String deptName) {
		super();
		this.deptName = deptName;

	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public JsonObject toJson() {
	    JsonObject json = new JsonObject();
	    if (id != null && !id.isEmpty()) {
				      json.put("_id", id);
				    }
	    	json.put("deptName", this.deptName)
	        ;
	    
	    return json;
	}
	public Dept(){}
	
	public Dept(JsonObject js){
		this.deptName = js.getString("deptName");
		this.id = js.getString("_id");
		
	}
}
