package com.campus.model;
import java.io.Serializable;
public class User implements Serializable {
 private long id; private String fullName,email,password,role,number,phone;
 public User(){}
 public User(long id,String fullName,String email,String password,String role,String number,String phone){this.id=id;this.fullName=fullName;this.email=email;this.password=password;this.role=role;this.number=number;this.phone=phone;}
 public long getId(){return id;} public void setId(long id){this.id=id;}
 public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPassword(){return password;} public void setPassword(String v){password=v;}
 public String getRole(){return role;} public void setRole(String v){role=v;}
 public String getNumber(){return number;} public void setNumber(String v){number=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
}
