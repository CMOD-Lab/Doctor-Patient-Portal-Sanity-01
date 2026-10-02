package com.hms.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.hms.util.AwsSecretsManagerUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cloud-ready database connection manager using HikariCP connection pooling
 * combined with Amazon RDS Proxy for optimized database connections.
 *
 * Cloud Readiness Fix (cr-java-0073 - Direct JDBC Connections):
 * Replaces raw DriverManager.getConnection() with HikariCP connection pool.
 * Database credentials are retrieved from AWS Secrets Manager instead of
 * being hardcoded. Connection parameters are driven by environment variables
 * to follow 12-factor app principles and support Amazon RDS Proxy.
 *
 * Cloud Readiness Fix (cr-java-0097 - Missing Connection Timeouts):
 * Adds explicit connection, socket, validation, and initialization timeouts
 * to the HikariCP pool configuration and the underlying MySQL JDBC driver.
 * This prevents connections from hanging indefinitely in cloud environments
 * with variable network latency or transient service failures.
 *
 * Environment variables consumed:
 *   DB_HOST     - RDS/RDS Proxy endpoint (default: localhost)
 *   DB_PORT     - Database port           (default: 3306)
 *   DB_NAME     - Database/schema name    (default: hospital)
 *   DB_USER     - Database username       (default: root)
 *   DB_PASSWORD - Database password; if DB_SECRET_NAME is set, the password
 *                 is fetched from AWS Secrets Manager instead.
 *   DB_SECRET_NAME - AWS Secrets Manager secret name for the DB password
 *                    (optional; takes precedence over DB_PASSWORD when set)
 *
 * HikariCP pool tuning environment variables (all optional):
 *   HIKARI_MAX_POOL_SIZE           (default: 10)
 *   HIKARI_MIN_IDLE                (default: 2)
 *   HIKARI_CONNECTION_TIMEOUT      (default: 30000 ms)  — max wait for pool to give a connection
 *   HIKARI_IDLE_TIMEOUT            (default: 600000 ms) — max time a connection sits idle
 *   HIKARI_MAX_LIFETIME            (default: 1800000 ms)— max lifetime of a pooled connection
 *   HIKARI_VALIDATION_TIMEOUT      (default: 5000 ms)   — max time to validate a connection
 *   HIKARI_INIT_FAIL_TIMEOUT       (default: 1 ms)      — fail fast if pool cannot init
 *   HIKARI_SOCKET_TIMEOUT          (default: 30000 ms)  — MySQL socket read/write timeout
 *   HIKARI_CONNECT_TIMEOUT         (default: 10000 ms)  — MySQL TCP connect timeout
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    /** Singleton HikariCP data source — initialised once on first use. */
    private static volatile HikariDataSource dataSource;

    /** Lock object for double-checked locking during pool initialisation. */
    private static final Object LOCK = new Object();

    // -----------------------------------------------------------------------
    // Environment-variable keys
    // -----------------------------------------------------------------------
    private static final String ENV_DB_HOST                    = "DB_HOST";
    private static final String ENV_DB_PORT                    = "DB_PORT";
    private static final String ENV_DB_NAME                    = "DB_NAME";
    private static final String ENV_DB_USER                    = "DB_USER";
    private static final String ENV_DB_PASSWORD                = "DB_PASSWORD";
    private static final String ENV_DB_SECRET_NAME             = "DB_SECRET_NAME";
    private static final String ENV_HIKARI_MAX_POOL_SIZE       = "HIKARI_MAX_POOL_SIZE";
    private static final String ENV_HIKARI_MIN_IDLE            = "HIKARI_MIN_IDLE";
    private static final String ENV_HIKARI_CONN_TIMEOUT        = "HIKARI_CONNECTION_TIMEOUT";
    private static final String ENV_HIKARI_IDLE_TIMEOUT        = "HIKARI_IDLE_TIMEOUT";
    private static final String ENV_HIKARI_MAX_LIFETIME        = "HIKARI_MAX_LIFETIME";
    // cr-java-0097: explicit timeout env vars
    private static final String ENV_HIKARI_VALIDATION_TIMEOUT  = "HIKARI_VALIDATION_TIMEOUT";
    private static final String ENV_HIKARI_INIT_FAIL_TIMEOUT   = "HIKARI_INIT_FAIL_TIMEOUT";
    private static final String ENV_HIKARI_SOCKET_TIMEOUT      = "HIKARI_SOCKET_TIMEOUT";
    private static final String ENV_HIKARI_CONNECT_TIMEOUT     = "HIKARI_CONNECT_TIMEOUT";

    // -----------------------------------------------------------------------
    // Default values
    // -----------------------------------------------------------------------
    private static final String DEFAULT_DB_HOST             = "localhost";
    private static final String DEFAULT_DB_PORT             = "3306";
    private static final String DEFAULT_DB_NAME             = "hospital";
    private static final String DEFAULT_DB_USER             = "root";
    private static final String DEFAULT_DB_PASSWORD         = "";
    private static final int    DEFAULT_MAX_POOL_SIZE       = 10;
    private static final int    DEFAULT_MIN_IDLE            = 2;
    private static final long   DEFAULT_CONN_TIMEOUT        = 30_000L;   // 30 s — max wait for pool connection
    private static final long   DEFAULT_IDLE_TIMEOUT        = 600_000L;  // 10 min
    private static final long   DEFAULT_MAX_LIFETIME        = 1_800_000L;// 30 min
    // cr-java-0097: timeout defaults
    private static final long   DEFAULT_VALIDATION_TIMEOUT  = 5_000L;    // 5 s  — connection liveness check
    private static final long   DEFAULT_INIT_FAIL_TIMEOUT   = 1L;        // fail fast on startup
    private static final int    DEFAULT_SOCKET_TIMEOUT      = 30_000;    // 30 s — MySQL socket read/write
    private static final int    DEFAULT_CONNECT_TIMEOUT     = 10_000;    // 10 s — MySQL TCP connect

    /** Utility class — prevent instantiation. */
    private DBConnection() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns a {@link Connection} from the HikariCP pool.
     *
     * <p>The pool is initialised lazily on the first call using a
     * double-checked locking pattern so that it is safe under concurrent
     * servlet requests.
     *
     * <p>Callers are responsible for closing the returned connection (which
     * returns it to the pool rather than closing the underlying socket).
     *
     * @return a pooled {@link Connection}, or {@code null} if the pool
     *         could not be initialised (error is logged)
     */
    public static Connection getConn() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain a connection from HikariCP pool", e);
            return null;
        }
    }

    /**
     * Shuts down the HikariCP pool gracefully.
     * Call this during application shutdown (e.g., from a
     * {@code ServletContextListener#contextDestroyed} callback).
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("HikariCP connection pool shut down.");
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the singleton {@link HikariDataSource}, creating it on first
     * call using double-checked locking.
     */
    private static HikariDataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (LOCK) {
                if (dataSource == null || dataSource.isClosed()) {
                    dataSource = buildDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Builds and configures a new {@link HikariDataSource} from environment
     * variables and (optionally) AWS Secrets Manager.
     *
     * cr-java-0097 fix: All timeout values are explicitly configured so that
     * no connection can hang indefinitely in a cloud environment:
     *   - connectionTimeout  : max time HikariCP waits to acquire a connection from the pool
     *   - validationTimeout  : max time HikariCP waits to validate a connection is alive
     *   - initializationFailTimeout : fail fast if the pool cannot be initialised at startup
     *   - connectTimeout (JDBC URL) : max time the MySQL driver waits to establish a TCP socket
     *   - socketTimeout  (JDBC URL) : max time the MySQL driver waits for a response on the socket
     */
    private static HikariDataSource buildDataSource() {
        String host     = getEnv(ENV_DB_HOST,     DEFAULT_DB_HOST);
        String port     = getEnv(ENV_DB_PORT,     DEFAULT_DB_PORT);
        String dbName   = getEnv(ENV_DB_NAME,     DEFAULT_DB_NAME);
        String user     = getEnv(ENV_DB_USER,     DEFAULT_DB_USER);
        String password = resolvePassword();

        // cr-java-0097: include connectTimeout and socketTimeout in the JDBC URL
        // so the MySQL driver enforces TCP-level timeouts independently of HikariCP.
        // connectTimeout — milliseconds to wait for the initial TCP connection.
        // socketTimeout  — milliseconds to wait for a response on an established socket.
        int connectTimeout = getEnvInt(ENV_HIKARI_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        int socketTimeout  = getEnvInt(ENV_HIKARI_SOCKET_TIMEOUT,  DEFAULT_SOCKET_TIMEOUT);

        // JDBC URL — compatible with Amazon RDS and RDS Proxy endpoints.
        // useSSL=true is recommended for RDS; allowPublicKeyRetrieval is
        // needed when connecting through RDS Proxy with IAM auth disabled.
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&connectTimeout=%d"
                + "&socketTimeout=%d",
                host, port, dbName, connectTimeout, socketTimeout);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool sizing — tunable via environment variables for cloud deployments
        config.setMaximumPoolSize(getEnvInt(ENV_HIKARI_MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE));
        config.setMinimumIdle(getEnvInt(ENV_HIKARI_MIN_IDLE, DEFAULT_MIN_IDLE));

        // cr-java-0097: explicit HikariCP timeout configuration
        // connectionTimeout: max ms HikariCP waits to hand out a connection from the pool
        config.setConnectionTimeout(getEnvLong(ENV_HIKARI_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT));
        // validationTimeout: max ms HikariCP waits to verify a connection is still alive
        config.setValidationTimeout(getEnvLong(ENV_HIKARI_VALIDATION_TIMEOUT, DEFAULT_VALIDATION_TIMEOUT));
        // initializationFailTimeout: negative = don't fail on startup; 0 = fail immediately if
        // no connection can be obtained; positive = wait up to N ms then fail.
        // Default 1 ms means fail fast so cloud health checks detect misconfiguration early.
        config.setInitializationFailTimeout(getEnvLong(ENV_HIKARI_INIT_FAIL_TIMEOUT, DEFAULT_INIT_FAIL_TIMEOUT));

        config.setIdleTimeout(getEnvLong(ENV_HIKARI_IDLE_TIMEOUT, DEFAULT_IDLE_TIMEOUT));
        config.setMaxLifetime(getEnvLong(ENV_HIKARI_MAX_LIFETIME, DEFAULT_MAX_LIFETIME));

        // Pool name for JMX / logging identification
        config.setPoolName("HmsHikariPool");

        // Connection health-check query
        config.setConnectionTestQuery("SELECT 1");

        // Recommended for RDS Proxy: keep-alive to avoid idle connection drops
        config.setKeepaliveTime(60_000L);

        LOGGER.info(String.format(
                "Initialising HikariCP pool — host: %s, port: %s, db: %s, maxPoolSize: %d, "
                + "connectionTimeout: %d ms, validationTimeout: %d ms, "
                + "connectTimeout: %d ms, socketTimeout: %d ms",
                host, port, dbName,
                getEnvInt(ENV_HIKARI_MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE),
                getEnvLong(ENV_HIKARI_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT),
                getEnvLong(ENV_HIKARI_VALIDATION_TIMEOUT, DEFAULT_VALIDATION_TIMEOUT),
                connectTimeout, socketTimeout));

        return new HikariDataSource(config);
    }

    /**
     * Resolves the database password.
     *
     * <p>If the {@code DB_SECRET_NAME} environment variable is set, the
     * password is fetched from AWS Secrets Manager (supports automatic
     * rotation). Otherwise, the value of {@code DB_PASSWORD} is used.
     */
    private static String resolvePassword() {
        String secretName = System.getenv(ENV_DB_SECRET_NAME);
        if (secretName != null && !secretName.trim().isEmpty()) {
            try {
                LOGGER.info("Fetching DB password from AWS Secrets Manager: " + secretName);
                return AwsSecretsManagerUtil.getSecret(secretName);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING,
                        "Could not retrieve secret from AWS Secrets Manager; falling back to DB_PASSWORD env var", e);
            }
        }
        return getEnv(ENV_DB_PASSWORD, DEFAULT_DB_PASSWORD);
    }

    // -----------------------------------------------------------------------
    // Environment-variable helpers
    // -----------------------------------------------------------------------

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private static int getEnvInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid integer value for env var " + key + ": " + value + ". Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    private static long getEnvLong(String key, long defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid long value for env var " + key + ": " + value + ". Using default: " + defaultValue);
            }
        }
        return defaultValue;
    }
}
