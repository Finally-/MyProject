package database;
import static java.lang.Class.forName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

class DbUtil implements AutoCloseable {

	private Connection conn;

	static {
		try {
			forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	DbUtil(String url) throws SQLException {
		conn = DriverManager.getConnection(url);
	}

	DbUtil(String url, String user, String password) throws SQLException {
		conn = DriverManager.getConnection(url, user, password);
	}

	DbUtil(String url, Properties info) throws SQLException {
		conn = DriverManager.getConnection(url, info);
	}

	ResultSet executeQuery(String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(sql);
	}

	ResultSet executeQuery(String sql, Object... params) throws SQLException {
		PreparedStatement ps = prepareStatement(sql, params);
		return ps.executeQuery();
	}

	int executeUpdate(String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(sql);
	}

	int executeUpdate(String sql, Object... params) throws SQLException {
		PreparedStatement ps = prepareStatement(sql, params);
		return ps.executeUpdate();
	}

	boolean execute(String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		return stmt.execute(sql);
	}

	boolean execute(String sql, Object... params) throws SQLException {
		PreparedStatement ps = prepareStatement(sql, params);
		return ps.execute();
	}

	private PreparedStatement prepareStatement(String sql, Object... params)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		for (int i = 0; i < params.length; i++)
			ps.setObject(i + 1, params[i]);
		return ps;
	}

	public void close() throws SQLException {
		if (conn != null)
			conn.close();
	}
}
