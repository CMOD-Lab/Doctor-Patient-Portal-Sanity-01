package com.hms.util;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Utility class for retrieving secrets from AWS Secrets Manager.
 *
 * Cloud Readiness Fix (cr-java-0113 - Lack of Externalized Secrets):
 * This utility replaces hardcoded credentials, API keys, and sensitive
 * configuration values embedded in source code or property files with
 * centralized secret management via AWS Secrets Manager. This enables:
 *   - Centralized secret lifecycle management
 *   - Automatic secret rotation
 *   - Audit logging of secret access
 *   - Elimination of credentials from source code and logs
 *
 * Cloud Readiness Fix (cr-java-0097 - Missing Connection Timeouts):
 * The AWS SDK SecretsManagerClient is now configured with explicit
 * apiCallTimeout and apiCallAttemptTimeout values so that calls to
 * AWS Secrets Manager cannot hang indefinitely in cloud environments
 * with variable network latency or transient service failures.
 *
 * Timeout environment variables (all optional):
 *   AWS_SDK_API_CALL_TIMEOUT         (default: 10000 ms) — total time budget for the entire API call
 *   AWS_SDK_API_CALL_ATTEMPT_TIMEOUT (default: 5000 ms)  — time budget per individual attempt/retry
 *
 * Usage example:
 *   String dbPassword = AwsSecretsManagerUtil.getSecret("hms/db/password");
 *   String apiKey     = AwsSecretsManagerUtil.getSecret("hms/api/key");
 *
 * The AWS region is resolved from the environment variable AWS_REGION,
 * falling back to "us-east-1" if not set.
 */
public class AwsSecretsManagerUtil {

    private static final Logger LOGGER = Logger.getLogger(AwsSecretsManagerUtil.class.getName());

    /** Environment variable name for the AWS region. */
    private static final String AWS_REGION_ENV = "AWS_REGION";

    /** Default AWS region used when AWS_REGION environment variable is not set. */
    private static final String DEFAULT_REGION = "us-east-1";

    // cr-java-0097: timeout environment variable keys
    private static final String ENV_API_CALL_TIMEOUT         = "AWS_SDK_API_CALL_TIMEOUT";
    private static final String ENV_API_CALL_ATTEMPT_TIMEOUT = "AWS_SDK_API_CALL_ATTEMPT_TIMEOUT";

    // cr-java-0097: default timeout values (milliseconds)
    /** Total time budget for the entire API call including all retries (10 seconds). */
    private static final long DEFAULT_API_CALL_TIMEOUT         = 10_000L;
    /** Time budget for each individual attempt / retry (5 seconds). */
    private static final long DEFAULT_API_CALL_ATTEMPT_TIMEOUT = 5_000L;

    private AwsSecretsManagerUtil() {
        // Utility class — do not instantiate
    }

    /**
     * Retrieves the plaintext value of a secret stored in AWS Secrets Manager.
     *
     * <p>cr-java-0097: The {@link SecretsManagerClient} is built with explicit
     * {@code apiCallTimeout} and {@code apiCallAttemptTimeout} values so that
     * the call cannot block indefinitely. Both values are configurable via
     * environment variables ({@code AWS_SDK_API_CALL_TIMEOUT} and
     * {@code AWS_SDK_API_CALL_ATTEMPT_TIMEOUT}).
     *
     * @param secretName the name or ARN of the secret to retrieve
     * @return the secret string value
     * @throws RuntimeException if the secret cannot be retrieved
     */
    public static String getSecret(String secretName) {
        String regionName = System.getenv(AWS_REGION_ENV);
        if (regionName == null || regionName.isEmpty()) {
            regionName = DEFAULT_REGION;
        }

        Region region = Region.of(regionName);

        // cr-java-0097: configure explicit API call timeouts on the AWS SDK client
        // to prevent indefinite hangs in cloud environments.
        long apiCallTimeout        = getEnvLong(ENV_API_CALL_TIMEOUT,         DEFAULT_API_CALL_TIMEOUT);
        long apiCallAttemptTimeout = getEnvLong(ENV_API_CALL_ATTEMPT_TIMEOUT, DEFAULT_API_CALL_ATTEMPT_TIMEOUT);

        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                // Total time budget for the entire call (all retries combined)
                .apiCallTimeout(Duration.ofMillis(apiCallTimeout))
                // Time budget for each individual attempt / retry
                .apiCallAttemptTimeout(Duration.ofMillis(apiCallAttemptTimeout))
                // Use default retry policy (3 retries with exponential back-off)
                .retryPolicy(RetryPolicy.defaultRetryPolicy())
                .build();

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .overrideConfiguration(overrideConfig)
                .build();

        LOGGER.info(String.format(
                "Retrieving secret '%s' from AWS Secrets Manager (region: %s, "
                + "apiCallTimeout: %d ms, apiCallAttemptTimeout: %d ms)",
                secretName, regionName, apiCallTimeout, apiCallAttemptTimeout));

        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            return response.secretString();

        } catch (SecretsManagerException e) {
            throw new RuntimeException(
                    "Failed to retrieve secret '" + secretName + "' from AWS Secrets Manager: "
                    + e.awsErrorDetails().errorMessage(), e);
        } finally {
            client.close();
        }
    }

    // -----------------------------------------------------------------------
    // Environment-variable helpers
    // -----------------------------------------------------------------------

    private static long getEnvLong(String key, long defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid long value for env var " + key + ": " + value
                        + ". Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }
}
