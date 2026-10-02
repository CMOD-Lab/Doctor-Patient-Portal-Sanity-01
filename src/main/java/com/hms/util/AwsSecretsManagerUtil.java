package com.hms.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for retrieving secrets from AWS Secrets Manager.
 *
 * <p>This class centralizes all credential and secret retrieval so that no
 * credentials are ever hard-coded in source code or configuration files.
 * Secrets are stored in AWS Secrets Manager and fetched at runtime using the
 * IAM role attached to the running compute resource (EC2, ECS, Lambda, etc.).</p>
 *
 * <p>Expected secret structure in AWS Secrets Manager (JSON format):
 * <pre>
 * {
 *   "db_url":      "jdbc:mysql://&lt;host&gt;:3306/hospital",
 *   "db_username": "root",
 *   "db_password": "your-db-password"
 * }
 * </pre>
 * </p>
 *
 * <p>The secret name is resolved from the environment variable
 * {@code APP_SECRET_NAME} (default: {@code hms/doctor-patient-portal/db}).
 * The AWS region is resolved from {@code AWS_REGION}
 * (default: {@code us-east-1}).</p>
 *
 * <p>The AWS SDK client is configured with explicit connection and socket
 * timeouts (cr-java-0097) to prevent indefinite hangs in cloud environments
 * with variable network latency or transient Secrets Manager API failures.
 * Timeout values are tunable via environment variables:
 * <ul>
 *   <li>{@code AWS_SM_CONN_TIMEOUT_MS}   – TCP connect timeout in ms (default: 5000)</li>
 *   <li>{@code AWS_SM_SOCKET_TIMEOUT_MS} – socket read timeout in ms (default: 10000)</li>
 *   <li>{@code AWS_SM_MAX_RETRIES}       – max retry attempts (default: 3)</li>
 * </ul>
 * </p>
 */
public class AwsSecretsManagerUtil {

    /** Environment variable that holds the AWS Secrets Manager secret name. */
    private static final String ENV_SECRET_NAME = "APP_SECRET_NAME";

    /** Environment variable that holds the AWS region. */
    private static final String ENV_AWS_REGION = "AWS_REGION";

    /** Default secret name used when {@code APP_SECRET_NAME} is not set. */
    private static final String DEFAULT_SECRET_NAME = "hms/doctor-patient-portal/db";

    /** Default AWS region used when {@code AWS_REGION} is not set. */
    private static final String DEFAULT_REGION = "us-east-1";

    /** JSON key for the database URL stored in the secret. */
    public static final String KEY_DB_URL = "db_url";

    /** JSON key for the database username stored in the secret. */
    public static final String KEY_DB_USERNAME = "db_username";

    /** JSON key for the database password stored in the secret. */
    public static final String KEY_DB_PASSWORD = "db_password";

    // -----------------------------------------------------------------------
    // Environment variable names for AWS SDK client timeout configuration
    // (cr-java-0097: Missing Connection Timeouts)
    // -----------------------------------------------------------------------

    /** Environment variable for the AWS SDK TCP connection timeout (ms). */
    private static final String ENV_SM_CONN_TIMEOUT_MS   = "AWS_SM_CONN_TIMEOUT_MS";

    /** Environment variable for the AWS SDK socket read timeout (ms). */
    private static final String ENV_SM_SOCKET_TIMEOUT_MS = "AWS_SM_SOCKET_TIMEOUT_MS";

    /** Environment variable for the AWS SDK maximum retry count. */
    private static final String ENV_SM_MAX_RETRIES       = "AWS_SM_MAX_RETRIES";

    /** Default TCP connection timeout for the Secrets Manager SDK client (5 s). */
    private static final int DEFAULT_SM_CONN_TIMEOUT_MS   = 5000;

    /** Default socket read timeout for the Secrets Manager SDK client (10 s). */
    private static final int DEFAULT_SM_SOCKET_TIMEOUT_MS = 10000;

    /** Default maximum retry attempts for the Secrets Manager SDK client. */
    private static final int DEFAULT_SM_MAX_RETRIES       = 3;

    private static volatile JsonNode cachedSecret = null;
    private static final Object LOCK = new Object();

    private AwsSecretsManagerUtil() {
        // Utility class — do not instantiate
    }

    /**
     * Retrieves the full secret JSON node from AWS Secrets Manager.
     * The result is cached for the lifetime of the JVM process to avoid
     * repeated API calls on every request.
     *
     * @return parsed {@link JsonNode} containing all secret key/value pairs
     * @throws RuntimeException if the secret cannot be retrieved or parsed
     */
    public static JsonNode getSecret() {
        if (cachedSecret == null) {
            synchronized (LOCK) {
                if (cachedSecret == null) {
                    cachedSecret = fetchSecret();
                }
            }
        }
        return cachedSecret;
    }

    /**
     * Convenience method to retrieve a single string value from the secret.
     *
     * @param key the JSON key within the secret (e.g. {@code "db_password"})
     * @return the string value associated with {@code key}
     * @throws RuntimeException if the key is not present in the secret
     */
    public static String getSecretValue(String key) {
        JsonNode secret = getSecret();
        if (!secret.has(key)) {
            throw new RuntimeException(
                    "Key '" + key + "' not found in AWS Secrets Manager secret: " + resolveSecretName());
        }
        return secret.get(key).asText();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static JsonNode fetchSecret() {
        String secretName = resolveSecretName();
        String region = resolveRegion();

        // -----------------------------------------------------------------------
        // Configure explicit connection and socket timeouts on the AWS SDK client
        // (cr-java-0097: Missing Connection Timeouts).
        //
        // Without these timeouts the SDK will block indefinitely if the Secrets
        // Manager endpoint is unreachable (e.g. VPC endpoint misconfiguration,
        // network partition, or transient AWS service disruption), exhausting
        // application threads and degrading overall service availability.
        // -----------------------------------------------------------------------
        ClientConfiguration clientConfig = new ClientConfiguration()
                // Maximum time (ms) to wait for the TCP connection to the
                // Secrets Manager endpoint to be established.
                .withConnectionTimeout(resolveIntEnv(ENV_SM_CONN_TIMEOUT_MS, DEFAULT_SM_CONN_TIMEOUT_MS))
                // Maximum time (ms) to wait for data to be returned on an
                // established socket (i.e. for the API response).
                .withSocketTimeout(resolveIntEnv(ENV_SM_SOCKET_TIMEOUT_MS, DEFAULT_SM_SOCKET_TIMEOUT_MS))
                // Number of retry attempts on transient failures (throttling,
                // 5xx errors) before propagating the exception to the caller.
                .withMaxErrorRetry(resolveIntEnv(ENV_SM_MAX_RETRIES, DEFAULT_SM_MAX_RETRIES));

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(clientConfig)
                .build();

        GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult result = client.getSecretValue(request);

        String secretString = result.getSecretString();
        if (secretString == null) {
            throw new RuntimeException(
                    "AWS Secrets Manager secret '" + secretName + "' does not contain a string value.");
        }

        try {
            return new ObjectMapper().readTree(secretString);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AWS Secrets Manager secret '" + secretName + "' as JSON.", e);
        }
    }

    private static String resolveSecretName() {
        String name = System.getenv(ENV_SECRET_NAME);
        return (name != null && !name.trim().isEmpty()) ? name.trim() : DEFAULT_SECRET_NAME;
    }

    private static String resolveRegion() {
        String region = System.getenv(ENV_AWS_REGION);
        return (region != null && !region.trim().isEmpty()) ? region.trim() : DEFAULT_REGION;
    }

    /**
     * Reads an integer value from an environment variable, falling back to
     * {@code defaultValue} when the variable is absent or non-numeric.
     */
    private static int resolveIntEnv(String envVar, int defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException ignored) {
                // Fall through to default
            }
        }
        return defaultValue;
    }
}
