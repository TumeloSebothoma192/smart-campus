package com.campus.model;
import java.io.Serializable; import java.time.LocalDateTime; import java.time.format.DateTimeFormatter;
public class RequestComment implements Serializable{private String author,text; private LocalDateTime createdAt=LocalDateTime.now(); public RequestComment(String a,String t){author=a;text=t;} public String getAuthor(){return author;} public String getText(){return text;} public String getFormattedCreatedAt(){return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));}}
