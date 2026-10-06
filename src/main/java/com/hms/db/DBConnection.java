package com.hms.db;

import com.hms.util.AwsSecretsManagerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides pooled database connections via HikariCP backed by Amazon RDS Proxy.
 *
 * <p>This class replaces the previous raw {@code DriverManager.getConnection()}
 * approach with a HikariCP connection pool, which is the recommended pattern for
 * cloud-native applications running on AWS. Database credentials and the JDBC URL
 * are retrieved securely from AWS Secrets Manager through
 * {@link AwsSecretsManagerUtil}, eliminating all hardcoded values.</p>
 *
 * <p>The pool is initialised once (lazy singleton) and reused for the lifetime of
 * the application. Callers are responsible for closing the {@link Connection}
 * returned by {@link #getConn()} so that it is returned to the pool.</p>
 *
 * <p>Key HikariCP timeout settings (cr-java-0097 – Missing Connection Timeouts):</p>
 * <ul>
 *   <li>{@code connectionTimeout}         – 30 s max wait to borrow a connection from the pool</li>
 *   <li>{@code idleTimeout}               – 600 s before an idle connection is retired</li>
 *   <li>{@code maxLifetime}               – 1800 s maximum lifetime per connection</li>
 *   <li>{@code keepaliveTime}             – 60 s interval for keepalive pings to prevent stale connections</li>
 *   <li>{@code initializationFailTimeout} – 1 ms (fail fast on startup if DB is unreachable)</li>
 *   <li>{@code validationTimeout}         – 5 s max time to validate a connection before use</li>
 *   <li>JDBC socket/connect timeouts      – set via {@code dataSource.socketTimeout} (60 s) and
 *       {@code dataSource.connectTimeout} (30 s) MySQL driver properties</li>
 *   <li>{@code maximumPoolSize}           – capped at 10 to stay within RDS Proxy limits</li>
 *   <li>{@code minimumIdle}              – 2 warm connections kept ready at all times</li>
 *   <li>{@code poolName}                 – "HmsHikariPool" for easy identification in logs</li>
 * </ul>
 */
public class DBConnection {

    /** Lazily-initialised HikariCP data source (thread-safe via volatile + double-checked locking). */
    private static volatile HikariDataSource dataSource;

    // Private constructor – utility class, not meant to be instantiated.
    private DBConnection() {
        throw new UnsupportedOperationException("DBConnection is a utility class.");
    }

    /**
     * Returns a {@link Connection} borrowed from the HikariCP pool.
     *
     * <p>The pool is created on the first call using credentials and the JDBC URL
     * fetched from AWS Secrets Manager. Subsequent calls reuse the same pool.</p>
     *
     * @return an active {@link Connection} from the pool, or {@code null} if the
     *         pool could not be initialised (error is printed to stderr)
     */
    public static Connection getConn() {
        try {
            if (dataSource == null) {
                synchronized (DBConnection.class) {
                    if (dataSource == null) {
                        dataSource = buildDataSource();
                    }
                }
            }
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs and configures a {@link HikariDataSource} using credentials
     * retrieved from AWS Secrets Manager and the JDBC URL stored in the same secret.
     *
     * <p>All timeout values are explicitly configured to prevent indefinite hangs
     * and resource exhaustion in cloud environments with variable network latency
     * (cr-java-0097 – Missing Connection Timeouts).</p>
     *
     * @return a fully configured, ready-to-use {@link HikariDataSource}
     */
    private static HikariDataSource buildDataSource() {
        // Retrieve connection details from AWS Secrets Manager (hms/db/credentials)
        String jdbcUrl  = AwsSecretsManagerUtil.getDbUrl();
        String username = AwsSecretsManagerUtil.getDbUsername();
        String password = AwsSecretsManagerUtil.getDbPassword();

        HikariConfig config = new HikariConfig();

        // JDBC driver and connection URL (points to Amazon RDS / RDS Proxy endpoint)
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // Pool sizing – tuned for RDS Proxy compatibility
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        // -----------------------------------------------------------------------
        // cr-java-0097 – Missing Connection Timeouts
        // Explicit timeout / lifetime settings to prevent indefinite hangs and
        // resource exhaustion in cloud environments with variable network latency.
        // All values are in milliseconds.
        // -----------------------------------------------------------------------

        // Maximum time (ms) a caller will wait to acquire a connection from the pool.
        // Prevents indefinite hangs when the pool is exhausted.
        config.setConnectionTimeout(30_000);   // 30 seconds

        // Maximum time (ms) a connection is allowed to sit idle in the pool before
        // being retired. Keeps the pool from holding stale connections.
        config.setIdleTimeout(600_000);        // 10 minutes

        // Maximum lifetime (ms) of a connection in the pool regardless of activity.
        // Must be shorter than any database or infrastructure-imposed connection limit.
        config.setMaxLifetime(1_800_000);      // 30 minutes

        // Interval (ms) at which HikariCP sends a keepalive ping to idle connections
        // to prevent them from being silently dropped by the network or RDS Proxy.
        config.setKeepaliveTime(60_000);       // 60 seconds

        // Maximum time (ms) HikariCP will wait for a connection to be validated
        // before treating it as broken. Must be less than connectionTimeout.
        config.setValidationTimeout(5_000);    // 5 seconds

        // Fail fast on startup: if the pool cannot obtain an initial connection
        // within this time the application will throw rather than start unhealthy.
        // A value of 1 means "fail immediately" (non-blocking startup check).
        config.setInitializationFailTimeout(1);

        // -----------------------------------------------------------------------
        // cr-java-0097 – MySQL driver-level socket and connect timeouts
        // These control the underlying TCP socket so that network-level hangs
        // (e.g., a silent firewall drop) are also bounded.
        // -----------------------------------------------------------------------

        // connectTimeout: max time (ms) the MySQL driver waits to establish the
        // initial TCP connection to the database server.
        config.addDataSourceProperty("connectTimeout", "30000");   // 30 seconds

        // socketTimeout: max time (ms) the MySQL driver waits for a response on
        // an already-established socket (i.e., query execution timeout at the
        // network layer). Prevents queries from hanging indefinitely.
        config.addDataSourceProperty("socketTimeout", "60000");    // 60 seconds

        // Validation query for MySQL – used by HikariCP to test connection liveness.
        config.setConnectionTestQuery("SELECT 1");

        // Pool identifier visible in logs and JMX for easy identification.
        config.setPoolName("HmsHikariPool");

        return new HikariDataSource(config);
    }
}
