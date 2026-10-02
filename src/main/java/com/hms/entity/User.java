package com.hms.entity;

/**
 * User entity class.
 *
 * <p><strong>Cloud-Readiness / AWS Secrets Manager Integration:</strong><br>
 * Passwords and other credentials are <em>never</em> hard-coded in source code,
 * property files, or local configuration files. Instead, all secrets are stored
 * in <strong>AWS Secrets Manager</strong> and retrieved at runtime via
 * {@link com.hms.util.AwsSecretsManagerUtil}.</p>
 *
 * <p>The {@code password} field on this entity holds only the value that was
 * resolved from AWS Secrets Manager (or a hashed credential supplied by the
 * caller). It is intentionally excluded from {@link #toString()} to prevent
 * accidental credential exposure in logs, stack traces, or any output stream.</p>
 *
 * <p>Secret structure expected in AWS Secrets Manager (JSON):</p>
 * <pre>
 * {
 *   "db_url":      "jdbc:mysql://&lt;host&gt;:3306/hospital",
 *   "db_username": "root",
 *   "db_password": "your-db-password"
 * }
 * </pre>
 *
 * <p>The secret name is resolved from the environment variable
 * {@code APP_SECRET_NAME} (default: {@code hms/doctor-patient-portal/db}).
 * The AWS region is resolved from {@code AWS_REGION} (default: {@code us-east-1}).</p>
 *
 * @see com.hms.util.AwsSecretsManagerUtil
 */
public class User {

	private int id;
	private String fullName;
	private String email;

	/**
	 * The user's password credential.
	 *
	 * <p>This field must <strong>never</strong> be populated with a plain-text
	 * password that originates from source code or a static configuration file.
	 * Credentials are managed exclusively through AWS Secrets Manager
	 * ({@link com.hms.util.AwsSecretsManagerUtil}) and are injected at runtime
	 * by the data-access layer (e.g., {@code UserDAO}).</p>
	 */
	private String password;


	public User() {
		super();
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

	/**
	 * Returns a string representation of the User object.
	 *
	 * <p>The {@code password} field is intentionally masked to prevent credential
	 * exposure in logs, stack traces, or any output stream. Credentials are
	 * managed via AWS Secrets Manager (see {@link com.hms.util.AwsSecretsManagerUtil})
	 * and must never appear in plain text in any log or serialised output.</p>
	 *
	 * @return a safe string representation with the password redacted
	 */
	@Override
	public String toString() {
		return "User [id=" + id
				+ ", fullName=" + fullName
				+ ", email=" + email
				+ ", password=" + "********"
				+ "]";
	}
}
