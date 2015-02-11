package database;

import static java.lang.Class.forName;
import static java.sql.DriverManager.getConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A delegation class of Connection.
 * 
 * @author Finally
 * @since 1.5
 */
class DbUtil implements AutoCloseable {

	private Connection conn;

	static final String IP = "192.168.16.200";
	static final int PORT = 3306;
	static final String USER = "park";
	static final String PASSWORD = "park";
	static final String DATABASE = "parkdb";

//	static final String IP = "localhost";
//	static final int PORT = 3306;
//	static final String USER = "root";
//	static final String PASSWORD = "";
//	static final String DATABASE = "parkdb";

	static {
		try {
			forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	DbUtil(String url) throws SQLException {
		conn = getConnection(url);
	}

	DbUtil(String url, String user, String password) throws SQLException {
		conn = getConnection(url, user, password);
	}

	DbUtil(String url, Properties info) throws SQLException {
		conn = getConnection(url, info);
	}

	ResultSet executeQuery(String sql) throws SQLException {
		return conn.createStatement().executeQuery(sql);
	}

	ResultSet executeQuery(String sql, Object... params) throws SQLException {
		return prepareStatement(sql, params).executeQuery();
	}

	int executeUpdate(String sql) throws SQLException {
		return conn.createStatement().executeUpdate(sql);
	}

	int executeUpdate(String sql, Object... params) throws SQLException {
		return prepareStatement(sql, params).executeUpdate();
	}

	boolean execute(String sql) throws SQLException {
		return conn.createStatement().execute(sql);
	}

	boolean execute(String sql, Object... params) throws SQLException {
		return prepareStatement(sql, params).execute();
	}

	private PreparedStatement prepareStatement(String sql, Object... params)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		if (ps.getParameterMetaData().getParameterCount() != params.length)
			throw new SQLException();
		for (int i = 0; i < params.length; i++)
			ps.setObject(i + 1, params[i]);
		return ps;
	}

	public void close() throws SQLException {
		if (conn != null)
			conn.close();
	}
}
