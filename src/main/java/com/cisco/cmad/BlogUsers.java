package com.cisco.cmad;


import org.mongodb.morphia.annotations.*;
import io.vertx.core.json.JsonObject;

@Entity(value="users")
public class BlogUsers {
	
	@Id
	private String id;
	private String userName;
	private String password;
	private String email;
	private String first;
	private String last;
	@Embedded
	private Company company;
	@Embedded
	private Dept dept;
	@Embedded
	private Site site;
	public Dept getDept() {
		return dept;
	}
	public void setDept(Dept dept) {
		this.dept = dept;
	}
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}
	public BlogUsers(){
		
	}
	public BlogUsers( String userName, String password, String email, String first, String last,Company company,Site site,Dept dept) {
		super();

		this.userName = userName;
		this.password = password;
		this.email = email;
		this.first = first;
		this.last = last;
		this.company = company;
		this.dept = dept;
		this.site = site;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		  if (id != null && !id.isEmpty()) {
		      json.put("_id", id);
		    }
		  json.put("userName", this.userName)
		  .put("password",this.password)
		  .put("email",this.email)
		  .put("first", this.first)
		  .put("last",last)
		  .put("company", company.toJson())
		  .put("site",site.toJson())
		  .put("dept", dept.toJson())
		  ;
		  return json;
	}

	public BlogUsers(JsonObject js){
		this.userName = js.getString("userName");
		this.id = js.getString("_id");
		this.password = js.getString("password");
		this.first = js.getString("first");
		this.last = js.getString("last");
		this.company  = new Company(js.getJsonObject("company"));
		this.dept = new Dept(js.getJsonObject("dept"));
		this.site = new Site(js.getJsonObject("site"));
		
	}

}
