package org.d2rq;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.d2rq.db.SQLScriptLoader;
import org.d2rq.db.vendor.Vendor;


/**
 * A simple wrapper around a HSQL in-memory database for testing purposes
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class HSQLDatabase {
	public final static String DRIVER_CLASS = org.hsqldb.jdbcDriver.class.getName();

	private final static String HSQL_USER = "d2rq";
	private final static String HSQL_PASS = "";
	
	private final String jdbcURL;
	private final Connection conn;
	
	public HSQLDatabase(String databaseName) {
		jdbcURL = "jdbc:hsqldb:mem:" + databaseName;
		try {
			Class.forName(DRIVER_CLASS);
			conn = DriverManager.getConnection(jdbcURL + ";create=true", 
					HSQL_USER, HSQL_PASS);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String getJdbcURL() {
		return jdbcURL;
	}
	
	public String getUser() {
		return HSQL_USER;
	}
	
	public String getPassword() {
		return HSQL_PASS;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void executeSQL(String sql) {
		new SQLScriptLoader(new StringReader(sql), conn).execute();
	}
	
	/**
	 * Reads SQL statements from a file, using the
	 * syntax defined in {@link SQLScriptLoader}.
	 * 
	 * @param filenameOrURL relative to current directory
	 */
	public void executeScript(String filenameOrURL) {
		try {
			if (filenameOrURL.matches("[a-zA-Z0-9]+:.*")) {	// Has protocol
				SQLScriptLoader.loadURI(new URI(filenameOrURL), conn);
			} else {
				SQLScriptLoader.loadFile(new File(filenameOrURL), conn);
			}
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public PreparedStatement prepareSQL(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	/**
	 * Returns the value of the first column of the first row of
	 * a <code>SELECT</code> SQL query result.
	 *  
	 * @param sql A <code>SELECT</code> query
	 * @return Value as a string, or null if there are zero 
	 * result rows or the value is <code>NULL</code>
	 */
	public String selectString(String sql) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) return null;
			String result = rs.getString(1);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Returns the value of the first column of the first row of
	 * a <code>SELECT</code> SQL query result.
	 *  
	 * @param sql A <code>SELECT</code> query
	 * @return Value as an object, or null if there are zero 
	 * result rows or the value is <code>NULL</code>
	 */
	public Object selectObject(String sql) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) return null;
			Object result = rs.getObject(1);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Returns the value of the first column of the first row of
	 * a <code>SELECT</code> SQL query result.
	 *  
	 * @param sql A <code>SELECT</code> query
	 * @return Value as a byte array, or null if there are zero 
	 * result rows or the value is <code>NULL</code>
	 */
	public byte[] selectBytes(String sql) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) return null;
			byte[] result = rs.getBytes(1);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Returns the class name of the SQL value in the first column of the first row of
	 * a <code>SELECT</code> SQL query result.
	 *  
	 * @param sql A <code>SELECT</code> query
	 * @return The class name of the value, or null if there are zero 
	 * result rows or the value is <code>NULL</code>
	 */
	public String selectClassName(String sql) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (!rs.next()) return null;
			String result = rs.getMetaData().getColumnClassName(1);
			rs.close();
			stmt.close();
			return result;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Drops all tables.
	 */
	public void clear() {
		executeSQL("DROP SCHEMA PUBLIC CASCADE");
	}
	
	public void close() {
		try {
			conn.close();
		} catch (SQLException ex) {
			// do nothing
		}
	}

	/**
	 * Closes the database.
	 * 
	 * @param dropAll Drops all tables if true.
	 */
	public void close(boolean dropAll) {
		if (dropAll) {
			clear();
		}
		close();
	}
	
	public Vendor vendor() {
		return Vendor.HSQLDB;
	}
}
