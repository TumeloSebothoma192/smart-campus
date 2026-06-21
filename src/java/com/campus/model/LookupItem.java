package com.campus.model;
import java.io.Serializable;
public class LookupItem implements Serializable{private long id; private String name; public LookupItem(long id,String name){this.id=id;this.name=name;} public long getId(){return id;} public long getCategoryId(){return id;} public long getDepartmentId(){return id;} public String getName(){return name;} public String getCategoryName(){return name;} public String getDepartmentName(){return name;} public String toString(){return name;}}
