package com.cisco.cmad;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
@Entity
public class BlogEntry {
	@Id
	private String id;
	private String content;
	private String title;
	private ArrayList<String> tags = new ArrayList<String>();
	private BlogUsers user;
	private String date ;
	private String type;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	@Reference
	private ArrayList<BlogEntry> comment= new ArrayList<BlogEntry>();
	
	@PrePersist void prePersist() {date = dateFormat.format(new Date());}
	public BlogEntry(String content, String tags,String title, BlogUsers user, String type) {
		super();
		this.content = content;
		this.title = title;
		List<String> temp = Arrays.asList(tags.split(","));
		this.tags.addAll(temp);
		this.user = user;
		this.type = type;
		this.date = dateFormat.format(new Date());
	}
	public BlogEntry(String content,  String tags,String title, BlogUsers user, Date date, String type, BlogEntry comment) {
		super();
		this.content = content;
		this.title = title;
		this.tags.add(tags);
		this.user = user;
		List<String> temp = Arrays.asList(tags.split(","));
		this.tags.addAll(temp);
		this.type = type;
		this.comment.add(comment);
		this.date = dateFormat.format(new Date());
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<String> getTags() {
		return tags;
	}
	public void addTags(String tags) {
		this.tags.add(tags);
	}
	public BlogUsers getUser() {
		return user;
	}
	public void setUser(BlogUsers user) {
		this.user = user;
	}
	public String getDate() {
		return date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ArrayList<BlogEntry> getComments() {
		return comment;
	}
	public void addComment(BlogEntry comment) {
		this.comment.add(comment);
	}
	
	public JsonArray commenttoJsonArray(){
		JsonArray jArray = new JsonArray();
		for (int i =0;i<comment.size();i++){
			jArray.add(comment.get(i).toJson());
		}
		return jArray;
	}
	public JsonArray tagstoJsonArray(){
		JsonArray jArray = new JsonArray();
		for (int i =0;i<tags.size();i++){
			jArray.add(tags.get(i));
		}
		return jArray;
	}
	public JsonObject toJson(){
		JsonObject json = new JsonObject();
	    if (id != null && !id.isEmpty()) {
		      json.put("_id", id);
		    }
	    
	    json.put("content", this.content)
	    .put("tags",tagstoJsonArray())
	    .put("title",this.title)
	    .put("user",user.toJson())
	    .put("type", this.type)
	    .put("date",this.date)
	    .put("comment",commenttoJsonArray() )	    
	    ;
		return json;
	}
	public BlogEntry(){}
	
	public BlogEntry(JsonObject js){
		this.id = js.getString("_id");
		this.content = js.getString("content");
		this.tags.addAll(js.getJsonArray("tags").getList());
		this.title = js.getString("title");
		this.user = new BlogUsers(js.getJsonObject("user"));
		this.type = js.getString("type");
		this.date = js.getString("date");
		JsonArray arr = js.getJsonArray("comment");
		for (int i =0;i<arr.size();i++){
		this.comment.add(new BlogEntry(arr.getJsonObject(i)));
		}
	}
}
