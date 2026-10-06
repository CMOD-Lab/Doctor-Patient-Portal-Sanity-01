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
 * <p>This class centralizes all credential and secret retrieval for the
 * Doctor-Patient Portal application, replacing hardcoded credentials with
 * secure, centrally managed secrets stored in AWS Secrets Manager.</p>
 *
 * <p>Secrets are stored under the following naming convention:
 * <ul>
 *   <li>{@code hms/db/credentials}  – database username and password</li>
 *   <li>{@code hms/user/credentials} – application-level user credential metadata</li>
 * </ul>
 * </p>
 *
 * <p>The AWS region is resolved from the environment variable
 * {@code AWS_REGION} (default: {@code us-east-1}).</p>
 *
 * <p>All AWS SDK client calls are configured with explicit connection, socket,
 * and request timeouts (cr-java-0097 – Missing Connection Timeouts) to prevent
 * indefinite hangs and resource exhaustion in cloud environments.</p>
 */
public class AwsSecretsManagerUtil {

    /** Environment variable that specifies the AWS region. */
    private static final String AWS_REGION_ENV = "AWS_REGION";

    /** Default AWS region when {@code AWS_REGION} is not set. */
    private static final String DEFAULT_REGION = "us-east-1";

    /** Secret name for database credentials stored in AWS Secrets Manager. */
    public static final String DB_SECRET_NAME = "hms/db/credentials";

    /** Secret name for user/application credentials stored in AWS Secrets Manager. */
    public static final String USER_SECRET_NAME = "hms/user/credentials";

    /** JSON key for the database username within the DB secret. */
    private static final String SECRET_KEY_DB_USERNAME = "username";

    /** JSON key for the database password within the DB secret. */
    private static final String SECRET_KEY_DB_PASSWORD = "password";

    /** JSON key for the database URL within the DB secret. */
    private static final String SECRET_KEY_DB_URL = "url";

    // -----------------------------------------------------------------------
    // AWS SDK client timeout constants (cr-java-0097 – Missing Connection Timeouts)
    // -----------------------------------------------------------------------

    /**
     * Maximum time (ms) to wait for the TCP connection to the AWS Secrets Manager
     * endpoint to be established. Prevents indefinite hangs when the endpoint is
     * unreachable (e.g., VPC misconfiguration, DNS failure).
     */
    private static final int AWS_CLIENT_CONNECTION_TIMEOUT_MS = 5_000;   // 5 seconds

    /**
     * Maximum time (ms) to wait for data on an already-established socket to the
     * AWS Secrets Manager endpoint. Bounds the time a single API call can block
     * waiting for a response.
     */
    private static final int AWS_CLIENT_SOCKET_TIMEOUT_MS = 10_000;      // 10 seconds

    /**
     * Maximum time (ms) for a complete API call (including retries) to the AWS
     * Secrets Manager endpoint. Acts as an overall ceiling on how long secret
     * retrieval can take.
     */
    private static final int AWS_CLIENT_REQUEST_TIMEOUT_MS = 15_000;     // 15 seconds

    /** Maximum number of retry attempts for transient AWS SDK errors. */
    private static final int AWS_CLIENT_MAX_RETRIES = 3;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Private constructor – utility class, not meant to be instantiated.
    private AwsSecretsManagerUtil() {
        throw new UnsupportedOperationException("AwsSecretsManagerUtil is a utility class.");
    }

    /**
     * Retrieves the raw secret string for the given secret name from AWS Secrets Manager.
     *
     * <p>The AWS SDK client is configured with explicit connection, socket, and
     * request timeouts to prevent indefinite hangs in cloud environments
     * (cr-java-0097 – Missing Connection Timeouts).</p>
     *
     * @param secretName the name or ARN of the secret to retrieve
     * @return the secret value as a plain string
     * @throws RuntimeException if the secret cannot be retrieved
     */
    public static String getSecret(String secretName) {
        String region = System.getenv(AWS_REGION_ENV);
        if (region == null || region.isEmpty()) {
            region = DEFAULT_REGION;
        }

        // cr-java-0097: Configure explicit timeouts on the AWS SDK client to prevent
        // indefinite hangs and resource exhaustion when the Secrets Manager endpoint
        // is slow or unreachable.
        ClientConfiguration clientConfig = new ClientConfiguration()
                // Maximum time (ms) to establish a TCP connection to the endpoint.
                .withConnectionTimeout(AWS_CLIENT_CONNECTION_TIMEOUT_MS)
                // Maximum time (ms) to wait for data on an established socket.
                .withSocketTimeout(AWS_CLIENT_SOCKET_TIMEOUT_MS)
                // Maximum time (ms) for a complete request (including retries).
                .withRequestTimeout(AWS_CLIENT_REQUEST_TIMEOUT_MS)
                // Maximum number of retry attempts for transient failures.
                .withMaxErrorRetry(AWS_CLIENT_MAX_RETRIES);

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(clientConfig)
                .build();

        GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult result = client.getSecretValue(request);
        return result.getSecretString();
    }

    /**
     * Retrieves the database username from AWS Secrets Manager.
     *
     * @return the database username stored under {@value #DB_SECRET_NAME}
     */
    public static String getDbUsername() {
        return getSecretField(DB_SECRET_NAME, SECRET_KEY_DB_USERNAME);
    }

    /**
     * Retrieves the database password from AWS Secrets Manager.
     *
     * @return the database password stored under {@value #DB_SECRET_NAME}
     */
    public static String getDbPassword() {
        return getSecretField(DB_SECRET_NAME, SECRET_KEY_DB_PASSWORD);
    }

    /**
     * Retrieves the database JDBC URL from AWS Secrets Manager.
     *
     * @return the JDBC URL stored under {@value #DB_SECRET_NAME}
     */
    public static String getDbUrl() {
        return getSecretField(DB_SECRET_NAME, SECRET_KEY_DB_URL);
    }

    /**
     * Parses a JSON secret and returns the value for the specified field key.
     *
     * @param secretName the name or ARN of the secret
     * @param fieldKey   the JSON key whose value should be returned
     * @return the string value of the specified field within the secret JSON
     * @throws RuntimeException if the secret cannot be parsed or the field is missing
     */
    private static String getSecretField(String secretName, String fieldKey) {
        try {
            String secretJson = getSecret(secretName);
            JsonNode rootNode = OBJECT_MAPPER.readTree(secretJson);
            JsonNode fieldNode = rootNode.get(fieldKey);
            if (fieldNode == null) {
                throw new RuntimeException(
                        "Field '" + fieldKey + "' not found in secret '" + secretName + "'");
            }
            return fieldNode.asText();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to retrieve field '" + fieldKey + "' from secret '" + secretName + "'", e);
        }
    }
}
