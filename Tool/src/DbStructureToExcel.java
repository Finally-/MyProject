import static java.lang.Class.forName;
import static java.sql.DriverManager.getConnection;
import static org.apache.poi.ss.usermodel.WorkbookFactory.create;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Fill MySQL database table structure data into a specified Excel file.
 * 
 * @author Finally
 * @since 1.7
 */
class DbStructureToExcel {
	
	static DbUtil dbu;

	static Connection conn;

	static String database;

	static Workbook wb;

	static OutputStream out;

	static {
		try {
			forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	static ResultSet executeQuery(String sql, Object... params)
			throws SQLException {
		PreparedStatement st = conn.prepareStatement(sql);
		for (int i = 0; i < params.length; i++)
			st.setObject(i + 1, params[i]);
		return st.executeQuery();
	}

	/**
	 * 
	 */
	static int getRow(ResultSet rs) throws SQLException {
		rs.last();
		return rs.getRow();
	}

	static void write() throws SQLException {
		String sql = "select TABLE_NAME,TABLE_COMMENT from information_schema.tables where TABLE_SCHEMA=?";
		ResultSet rs = executeQuery(sql, database);
		int sheetnum = 1;
		while (rs.next()) {
			Sheet sheet = wb.cloneSheet(0);
			String tableName = rs.getString("TABLE_NAME");
			wb.setSheetName(sheetnum, tableName);
			Row row1 = sheet.getRow(1);
			row1.createCell(3).setCellValue(tableName);
			row1.createCell(5).setCellValue(rs.getString("TABLE_COMMENT"));
			write(tableName);
			sheetnum++;
		}
		wb.setSheetHidden(0, true);
	}

	static void write(String tableName) throws SQLException {
		String sql = "select ORDINAL_POSITION,COLUMN_NAME,COLUMN_TYPE,COLUMN_COMMENT,COLUMN_DEFAULT,IS_NULLABLE "
				+ " from information_schema.columns where TABLE_SCHEMA=? and TABLE_NAME=?";
		ResultSet rs = executeQuery(sql, database, tableName);
		int rownum = 5;
		while (rs.next()) {
			Row row = wb.getSheet(tableName).createRow(rownum);
			row.createCell(0).setCellValue(rs.getInt("ORDINAL_POSITION"));
			row.createCell(1).setCellValue(rs.getString("COLUMN_NAME"));
			row.createCell(2).setCellValue(rs.getString("COLUMN_TYPE"));
			row.createCell(3).setCellValue(rs.getString("COLUMN_COMMENT"));
			row.createCell(4).setCellValue(rs.getString("IS_NULLABLE"));
			Object dftValue = rs.getObject("COLUMN_DEFAULT");
			if (dftValue != null)
				row.createCell(5).setCellValue(dftValue.toString());
			rownum++;
		}
	}

	/**
	 * One table can only have one primary key constraint, while one primary key
	 * constraint may contains more than one columns.
	 */
	static void writePrimaryKey(String tableName) throws SQLException {
		String sql = "select COLUMN_NAME from information_schema.key_column_usage kcu "
				+ " join information_schema.table_constraints tc on kcu.TABLE_SCHEMA=tc.TABLE_SCHEMA "
				+ " and kcu.TABLE_NAME=tc.TABLE_NAME and kcu.TABLE_CONSTRAINTS=tc.TABLE_CONSTRAINTS "
				+ " where tc.TABLE_SCHEMA=? and tc.TABLE_NAME=? and CONSTRAINT_TYPE='PRIMARY KEY'";
		ResultSet rs = executeQuery(sql, database, tableName);
		StringBuilder primaryKey = new StringBuilder();
		while (rs.next())
			primaryKey.append(rs.getString("COLUMN_NAME")).append(",");
		if (primaryKey.length() != 0)
			primaryKey.deleteCharAt(primaryKey.length() - 1);
		wb.getSheet(tableName).getRow(1).createCell(1)
				.setCellValue(primaryKey.toString());
	}

	/**
	 * Note that a unique constraint can contains more than one column, and a
	 * table may have more than one unique constraint, but different unique
	 * constraint has different names.
	 */
	static void writeUniqueKey(String tableName) throws SQLException {
		String sql = "select CONSTRAINT_NAME from information_schema.table_constraints "
				+ " where TABLE_SCHEMA=? and TABLE_NAME=? and CONSTRAINT_TYPE='UNIQUE'";
		ResultSet rs = executeQuery(sql, database, tableName);
		StringBuilder unique = new StringBuilder();
		while (rs.next())
			unique.append(
					writeUniqueKey(tableName, rs.getString("CONSTRAINT_NAME")))
					.append(",");
		if (unique.length() != 0)
			unique.deleteCharAt(unique.length() - 1);
		// TODO Write Unique key info to Excel.
	}

	static String writeUniqueKey(String tableName, String constraintName)
			throws SQLException {
		String sql = "select COLUMN_NAME from information_schema.key_column_usage "
				+ " where TABLE_SCHEMA=? and TABLE_NAME=? and CONSTRAINT_NAME=?";
		ResultSet rs = executeQuery(sql, database, tableName, constraintName);
		int rownum = getRow(rs);
		if (rownum == 1)
			return rs.getString("COLUMN_NAME");
		rs.beforeFirst();// must move the cursor to initial position
		StringBuilder unique = new StringBuilder("(");
		while (rs.next())
			unique.append(rs.getString("COLUMN_NAME")).append(",");
		unique.deleteCharAt(unique.length() - 1).append(")");
		return unique.toString();
	}

	static void writeForeignKey(String tableName) throws SQLException {
		String sql = "select COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from information_schema.key_column_usage kcu "
				+ " join information_schema.table_constraints tc on kcu.TABLE_SCHEMA=tc.TABLE_SCHEMA "
				+ " and kcu.TABLE_NAME=tc.TABLE_NAME and kcu.TABLE_CONSTRAINTS=tc.TABLE_CONSTRAINTS "
				+ " where tc.TABLE_SCHEMA=? and tc.TABLE_NAME=? and CONSTRAINT_TYPE='FOREIGN KEY'";
		ResultSet rs = executeQuery(sql, database, tableName);
		StringBuilder foreignKey = new StringBuilder();
		while (rs.next()) {
			foreignKey.append(rs.getString("COLUMN_NAME"))
					.append(" REFERENCES ")
					.append(rs.getString("REFERENCED_TABLE_NAME")).append("(")
					.append(rs.getString("REFERENCED_COLUMN_NAME"))
					.append("),");
		}
		if (foreignKey.length() != 0)
			foreignKey.deleteCharAt(foreignKey.length() - 1);
		// TODO Write foreign key info to Excel.
	}

	static void close(AutoCloseable... resources) {
		for (AutoCloseable resource : resources)
			if (resource != null)
				try {
					resource.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	}

	public static void main(String[] args) throws IOException, SQLException,
			InvalidFormatException {
		String ip = "localhost";
		int port = 3306;
		String user = "root";
		String password = "";
		database = "";
		// A raw simulation of MySQL style commands.
		// Not tested yet.
		for (String param : args) {
			switch (param.substring(0, 2)) {
			case "-h":
				ip = param.substring(2).trim();
				break;
			case "-P":
				port = Integer.parseInt(param.substring(2).trim());
				break;
			case "-u":
				user = param.substring(2).trim();
				break;
			case "-p":
				password = param.substring(2).trim();
				break;
			case "-D":
				database = param.substring(2).trim();
				break;
			default:
				throw new IllegalArgumentException(param);
			}
		}
		String url = "jdbc:mysql://" + ip + ":" + port + "/" + database;
		File target = new File("D:/Documents/" + database + ".xlsx");
		try {
			conn = getConnection(url, user, password);
			out = new FileOutputStream(target);
			wb = create(target);
			try (InputStream in = new FileInputStream("src/DbStructure.xlsx");) {
				byte[] bytes = new byte[in.available()];
				in.read(bytes);
				out.write(bytes);
			}
			write();
			wb.write(out);
		} finally {
			close(wb, out, conn);
		}
	}
}
