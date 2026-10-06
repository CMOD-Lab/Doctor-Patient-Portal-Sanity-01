package com.hms.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;

/**
 * Utility class for retrieving secrets from AWS Secrets Manager.
 *
 * Cloud Readiness Fix (cr-java-0113 - Lack of Externalized Secrets):
 * Replaces all hardcoded credentials, API keys, and sensitive configuration
 * values with centralized secret retrieval from AWS Secrets Manager.
 * This enables:
 *   - Centralized secret management across all environments
 *   - Automatic secret rotation without code changes
 *   - Full audit logging of secret access via AWS CloudTrail
 *   - Elimination of credentials from source code and property files
 *
 * Usage:
 *   String dbPassword = AwsSecretsManagerUtil.getSecret("hms/db/password");
 *   String apiKey     = AwsSecretsManagerUtil.getSecret("hms/api/key");
 */
public class AwsSecretsManagerUtil {

    /**
     * AWS region is read from the environment variable AWS_REGION (or
     * AWS_DEFAULT_REGION) so that no region is hardcoded in source code.
     * Falls back to "us-east-1" when neither variable is set.
     */
    private static final String REGION =
            System.getenv("AWS_REGION") != null ? System.getenv("AWS_REGION")
            : System.getenv("AWS_DEFAULT_REGION") != null ? System.getenv("AWS_DEFAULT_REGION")
            : "us-east-1";

    /**
     * Retrieves the plaintext value of a secret stored in AWS Secrets Manager.
     *
     * @param secretName the name or ARN of the secret (e.g. "hms/db/password")
     * @return the secret string value
     * @throws RuntimeException if the secret cannot be retrieved
     */
    public static String getSecret(String secretName) {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(REGION)
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);

        GetSecretValueResult getSecretValueResult;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("AWS Secrets Manager: secret not found: " + secretName, e);
        } catch (InvalidRequestException e) {
            throw new RuntimeException("AWS Secrets Manager: invalid request for secret: " + secretName, e);
        } catch (InvalidParameterException e) {
            throw new RuntimeException("AWS Secrets Manager: invalid parameter for secret: " + secretName, e);
        }

        if (getSecretValueResult.getSecretString() != null) {
            return getSecretValueResult.getSecretString();
        }

        throw new RuntimeException(
                "AWS Secrets Manager: secret '" + secretName + "' does not contain a string value.");
    }
}
