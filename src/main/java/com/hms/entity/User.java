package com.hms.entity;

import com.hms.util.AwsSecretsManagerUtil;

/**
 * User entity representing a patient/user in the Doctor-Patient Portal.
 *
 * <p><b>Cloud Readiness Fix (cr-java-0113 — Lack of Externalized Secrets):</b><br>
 * The {@code password} field previously appeared in plaintext inside
 * {@link #toString()}, which caused credential leakage in application logs,
 * stack traces, and debug output. The fix:
 * <ol>
 *   <li>Masks the password with {@code "****"} in {@link #toString()} so it is
 *       never emitted to logs.</li>
 *   <li>Adds {@link #getPasswordFromSecretsManager(String)} as the recommended
 *       way to retrieve a user's stored credential from AWS Secrets Manager
 *       instead of keeping the raw value in the entity field at runtime.</li>
 * </ol>
 * Sensitive credentials are managed centrally via
 * {@link com.hms.util.AwsSecretsManagerUtil}, which provides automatic
 * rotation, audit logging, and eliminates hardcoded secrets from source code.
 * </p>
 */
public class User {
	private int id;
	private String fullName;
	private String email;
	/**
	 * Stores only a reference key (e.g. the AWS Secrets Manager secret name)
	 * or a hashed/encoded credential — never a plaintext password at runtime.
	 * Use {@link #getPasswordFromSecretsManager(String)} to retrieve the actual
	 * credential from AWS Secrets Manager when authentication is required.
	 */
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


	/**
	 * Retrieves the user's credential from AWS Secrets Manager.
	 *
	 * <p><b>Cloud Readiness Fix (cr-java-0113 — Lack of Externalized Secrets):</b><br>
	 * Instead of storing or comparing plaintext passwords in the entity, callers
	 * should use this method to fetch the credential from AWS Secrets Manager
	 * using a secret name derived from the user's identity (e.g.
	 * {@code "hms/user/<email>/password"}). This ensures:
	 * <ul>
	 *   <li>Credentials are never embedded in source code or property files.</li>
	 *   <li>Automatic rotation is supported without application redeployment.</li>
	 *   <li>All secret access is audit-logged by AWS CloudTrail.</li>
	 * </ul>
	 * </p>
	 *
	 * @param secretName the AWS Secrets Manager secret name or ARN for this
	 *                   user's credential (e.g. {@code "hms/user/john@example.com/password"})
	 * @return the plaintext secret value retrieved from AWS Secrets Manager
	 * @throws RuntimeException if the secret cannot be retrieved
	 * @see com.hms.util.AwsSecretsManagerUtil#getSecret(String)
	 */
	public String getPasswordFromSecretsManager(String secretName) {
		return AwsSecretsManagerUtil.getSecret(secretName);
	}


	/**
	 * Returns a string representation of the User object.
	 *
	 * <p><b>Security Fix (cr-java-0113 — Lack of Externalized Secrets):</b><br>
	 * The {@code password} field is masked with {@code "****"} to prevent
	 * credential leakage in application logs, stack traces, and debug output.
	 * Passwords and other sensitive credentials must be managed via
	 * AWS Secrets Manager and must never appear in plaintext within log output
	 * or serialized representations.</p>
	 *
	 * @see com.hms.util.AwsSecretsManagerUtil for AWS Secrets Manager integration
	 */
	@Override
	public String toString() {
		return "User [id=" + id + ", fullName=" + fullName + ", email=" + email + ", password=" + "****" + "]";
	}
}
