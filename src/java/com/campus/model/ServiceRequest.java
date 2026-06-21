package com.campus.model;
import java.io.Serializable; import java.time.LocalDateTime; import java.time.format.DateTimeFormatter; import java.util.*;
public class ServiceRequest implements Serializable{
 private long id,createdById; private String title,description,priority,status,createdByName; private LookupItem category,department; private LocalDateTime createdAt=LocalDateTime.now(),updatedAt=LocalDateTime.now(); private List<RequestComment> comments=new ArrayList<>();
 public ServiceRequest(){}
 public ServiceRequest(long id,long uid,String name,String title,String description,String category,String department){this.id=id;createdById=uid;createdByName=name;this.title=title;this.description=description;this.category=new LookupItem(0,category);this.department=new LookupItem(0,department);priority="MEDIUM";status="PENDING";}
 public long getId(){return id;} public long getRequestId(){return id;} public void setId(long id){this.id=id;}
 public long getCreatedById(){return createdById;} public String getCreatedByName(){return createdByName;}
 public String getTitle(){return title;} public void setTitle(String v){title=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public LookupItem getCategory(){return category;} public void setCategory(String v){category=new LookupItem(0,v);}
 public LookupItem getDepartment(){return department;} public void setDepartment(String v){department=new LookupItem(0,v);}
 public String getCategoryName(){return category==null?null:category.getName();}
 public String getDepartmentName(){return department==null?null:department.getName();}
 public String getPriority(){return priority;} public void setPriority(String v){priority=v;updatedAt=LocalDateTime.now();}
 public String getStatus(){return status;} public void setStatus(String v){status=v;updatedAt=LocalDateTime.now();}
 public LocalDateTime getCreatedAt(){return createdAt;} public String getFormattedCreatedAt(){return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));}
 public String getFormattedUpdatedAt(){return updatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));}
 public List<RequestComment> getComments(){return comments;}
}
