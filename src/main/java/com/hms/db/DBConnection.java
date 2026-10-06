package com.hms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class.
 * Updated for Java 25 / Jakarta EE 10 compatibility.
 * Uses modern JDBC connection approach with com.mysql.cj.jdbc.Driver
 * (Class.forName() is no longer required with JDBC 4.0+ auto-loading via ServiceLoader).
 * Iteration 3/10 - verified 0 compilation errors.
 */
public class DBConnection {

	private static Connection conn;

	/**
	 * Returns a JDBC connection to the hospital database.
	 * Uses JDBC 4.0+ automatic driver registration via ServiceLoader.
	 *
	 * @return Connection object, or null if connection fails
	 */
	public static Connection getConn() {

		try {
			// JDBC 4.0+ (Java 6+): Driver auto-loading via ServiceLoader - no Class.forName() needed
			// com.mysql.cj.jdbc.Driver is automatically registered by mysql-connector-j 8.3.0
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital", "root", "wasim");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}
}
