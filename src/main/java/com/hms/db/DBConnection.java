package com.hms.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.hms.util.AwsSecretsManagerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides pooled JDBC {@link Connection} instances via
 * <strong>HikariCP</strong>, the high-performance connection-pooling library.
 *
 * <p><strong>Fix for cr-java-0097 (Missing Connection Timeouts):</strong>
 * The original implementation used a bare {@code DriverManager.getConnection()}
 * call (line 18 of the source) with no timeout configuration whatsoever.
 * In cloud environments with variable network latency and potential service
 * failures, this caused connections to hang indefinitely, exhausting threads
 * and degrading performance.  This class replaces that pattern with HikariCP
 * and configures explicit timeouts at both the pool level and the JDBC driver
 * level so that every network operation is bounded.</p>
 *
 * <p>Credentials are resolved at runtime from <strong>AWS Secrets Manager</strong>
 * via {@link AwsSecretsManagerUtil} — no database URL, username, or password is
 * hard-coded in this class.</p>
 *
 * <p>When deployed behind <strong>Amazon RDS Proxy</strong>, point
 * {@code db_url} in the secret to the RDS Proxy endpoint instead of the
 * RDS instance endpoint.  HikariCP's connection pool then multiplexes
 * application connections through the proxy, reducing the number of
 * connections that reach the database engine and enabling seamless
 * failover.</p>
 *
 * <p>Expected secret JSON structure in AWS Secrets Manager:
 * <pre>
 * {
 *   "db_url":      "jdbc:mysql://&lt;rds-proxy-endpoint&gt;:3306/hospital",
 *   "db_username": "admin",
 *   "db_password": "your-secure-password"
 * }
 * </pre>
 * </p>
 *
 * <p>Optional HikariCP tuning via environment variables:
 * <ul>
 *   <li>{@code DB_POOL_MAX_SIZE}          – maximum pool size (default: 10)</li>
 *   <li>{@code DB_POOL_MIN_IDLE}          – minimum idle connections (default: 2)</li>
 *   <li>{@code DB_CONN_TIMEOUT_MS}        – connection timeout in ms (default: 30000)</li>
 *   <li>{@code DB_IDLE_TIMEOUT_MS}        – idle timeout in ms (default: 600000)</li>
 *   <li>{@code DB_MAX_LIFETIME_MS}        – max connection lifetime in ms (default: 1800000)</li>
 *   <li>{@code DB_SOCKET_TIMEOUT_MS}      – JDBC socket (read) timeout in ms (default: 60000)</li>
 *   <li>{@code DB_CONNECT_TIMEOUT_MS}     – JDBC TCP connect timeout in ms (default: 10000)</li>
 *   <li>{@code DB_KEEPALIVE_TIME_MS}      – HikariCP keepalive interval in ms (default: 300000)</li>
 *   <li>{@code DB_LEAK_DETECTION_MS}      – connection leak detection threshold in ms (default: 60000)</li>
 *   <li>{@code DB_INIT_FAIL_TIMEOUT_MS}   – pool init failure timeout in ms (default: 1)</li>
 * </ul>
 * </p>
 */
public class DBConnection {

    // -------------------------------------------------------------------------
    // HikariCP DataSource — initialised once, shared across all requests
    // -------------------------------------------------------------------------

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    /**
     * Returns a {@link Connection} obtained from the HikariCP connection pool.
     *
     * <p>The pool is lazily initialised on the first call and reused for all
     * subsequent calls.  Callers are responsible for closing the returned
     * connection (which returns it to the pool rather than closing the
     * underlying physical connection).</p>
     *
     * @return a pooled {@link Connection} ready for use
     */
    public static Connection getConn() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    dataSource = buildDataSource();
                }
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to obtain a connection from the HikariCP pool.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds and configures the {@link HikariDataSource} using credentials
     * retrieved from AWS Secrets Manager and optional environment-variable
     * overrides for pool-sizing and timeout parameters.
     *
     * <p><strong>cr-java-0097 remediation — Configure connection timeouts for
     * AWS SDK clients and HTTP libraries:</strong>
     * Explicit timeouts are configured at two levels:
     * <ol>
     *   <li><strong>HikariCP level</strong> – controls how long the pool waits
     *       to acquire a connection ({@code connectionTimeout}), how long idle
     *       connections are retained ({@code idleTimeout}), the maximum lifetime
     *       of a connection ({@code maxLifetime}), keepalive ping frequency
     *       ({@code keepaliveTime}), and connection-leak detection threshold
     *       ({@code leakDetectionThreshold}).</li>
     *   <li><strong>JDBC / MySQL driver level</strong> – controls the TCP
     *       connect timeout ({@code connectTimeout}) and the socket read timeout
     *       ({@code socketTimeout}) so that network hangs at the driver layer
     *       are also bounded, preventing indefinite blocking in cloud
     *       environments with variable network latency.</li>
     * </ol>
     * </p>
     */
    private static HikariDataSource buildDataSource() {
        // Retrieve connection credentials from AWS Secrets Manager.
        // No credentials are hard-coded; they are fetched securely at runtime.
        String dbUrl      = AwsSecretsManagerUtil.getSecretValue(AwsSecretsManagerUtil.KEY_DB_URL);
        String dbUsername = AwsSecretsManagerUtil.getSecretValue(AwsSecretsManagerUtil.KEY_DB_USERNAME);
        String dbPassword = AwsSecretsManagerUtil.getSecretValue(AwsSecretsManagerUtil.KEY_DB_PASSWORD);

        HikariConfig config = new HikariConfig();

        // Core connection settings
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);

        // Pool sizing — tunable via environment variables for cloud deployments
        config.setMaximumPoolSize(resolveIntEnv("DB_POOL_MAX_SIZE", 10));
        config.setMinimumIdle(resolveIntEnv("DB_POOL_MIN_IDLE", 2));

        // -----------------------------------------------------------------------
        // HikariCP-level timeout settings (cr-java-0097: Missing Connection Timeouts)
        // -----------------------------------------------------------------------

        // Maximum milliseconds to wait for a connection from the pool before
        // throwing a SQLException. Prevents indefinite hangs when the pool is
        // exhausted in cloud environments.
        config.setConnectionTimeout(resolveIntEnv("DB_CONN_TIMEOUT_MS", 30000));

        // Maximum milliseconds a connection may sit idle in the pool before
        // being eligible for eviction. Must be < maxLifetime.
        config.setIdleTimeout(resolveIntEnv("DB_IDLE_TIMEOUT_MS", 600000));

        // Maximum lifetime of a connection in the pool. Connections older than
        // this are retired and replaced, preventing stale connections to RDS
        // after failover or maintenance events.
        config.setMaxLifetime(resolveIntEnv("DB_MAX_LIFETIME_MS", 1800000));

        // Frequency at which HikariCP sends a keepalive ping to idle connections
        // to prevent them from being silently dropped by AWS network ACLs or
        // RDS Proxy idle-connection timeouts. Must be < idleTimeout.
        config.setKeepaliveTime(resolveIntEnv("DB_KEEPALIVE_TIME_MS", 300000));

        // If a connection is borrowed from the pool and not returned within this
        // threshold, HikariCP logs a warning (and stack trace) to help detect
        // connection leaks in cloud-deployed services.
        config.setLeakDetectionThreshold(resolveIntEnv("DB_LEAK_DETECTION_MS", 60000));

        // Controls how long HikariCP waits during pool initialisation before
        // giving up. Setting to 1 ms causes the pool to fail fast on startup
        // if the database is unreachable, enabling cloud health checks to
        // detect the problem immediately.
        config.setInitializationFailTimeout(resolveIntEnv("DB_INIT_FAIL_TIMEOUT_MS", 1));

        // -----------------------------------------------------------------------
        // JDBC / MySQL driver-level timeout settings (cr-java-0097)
        // These bound network-level operations independently of HikariCP so that
        // a hung TCP connection at the driver layer does not block indefinitely.
        // -----------------------------------------------------------------------
        Properties dsProps = new Properties();

        // Maximum time (ms) the MySQL driver waits to establish a TCP connection
        // to the database server. Prevents indefinite blocking during network
        // partitions or DNS resolution failures in cloud VPCs.
        dsProps.setProperty("connectTimeout",
                String.valueOf(resolveIntEnv("DB_CONNECT_TIMEOUT_MS", 10000)));

        // Maximum time (ms) the MySQL driver waits for data on an established
        // socket (i.e. for a query response). Prevents long-running or hung
        // queries from blocking application threads indefinitely.
        dsProps.setProperty("socketTimeout",
                String.valueOf(resolveIntEnv("DB_SOCKET_TIMEOUT_MS", 60000)));

        // Enable TCP keepalive at the OS level to detect dead connections
        // that may occur due to AWS network infrastructure timeouts.
        dsProps.setProperty("tcpKeepAlive", "true");

        config.setDataSourceProperties(dsProps);

        // Pool name for monitoring / CloudWatch metrics
        config.setPoolName("HmsHikariPool");

        // Validation query for MySQL / RDS Proxy health checks
        config.setConnectionTestQuery("SELECT 1");

        // RDS Proxy: auto-commit enabled to allow the proxy to pin connections
        // only when necessary, improving multiplexing efficiency
        config.setAutoCommit(true);

        return new HikariDataSource(config);
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
