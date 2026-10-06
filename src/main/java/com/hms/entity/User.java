package com.hms.entity;

/**
 * User entity class.
 * NOTE: The password field is managed via AWS Secrets Manager and is intentionally
 * excluded from toString() to prevent credential exposure in logs and audit trails.
 */
public class User {
	private int id;
	private String fullName;
	private String email;
	private String password;
	
	
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}


	public User(int id, String fullName, String email, String password) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}


	public User(String fullName, String email, String password) {
		super();
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getFullName() {
		return fullName;
	}


	public void setFullName(String fullName) {
		this.fullName = fullName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString() {
		// Password is intentionally excluded from toString() to prevent credential
		// exposure in application logs, stack traces, and audit trails.
		// Credentials are managed via AWS Secrets Manager (secret name: hms/user/credentials).
		return "User [id=" + id + ", fullName=" + fullName + ", email=" + email + ", password=***REDACTED***]";
	}
	
	
	
	
	
	
	
	
}
