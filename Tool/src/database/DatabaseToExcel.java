package database;

import static database.DbUtil.DATABASE;
import static database.DbUtil.IP;
import static database.DbUtil.PASSWORD;
import static database.DbUtil.PORT;
import static database.DbUtil.USER;
import static database.Utils.close;
import static java.lang.System.out;
import static org.apache.poi.ss.usermodel.WorkbookFactory.create;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * @author Finally
 * @since 1.8
 */
class DatabaseToExcel {

	static DbUtil dbu;

	static Workbook structureWb;

	static Workbook dataWb;

	static void write() throws SQLException {
		String sql = "select TABLE_NAME,TABLE_COMMENT from information_schema.tables where TABLE_SCHEMA=?";
		ResultSet rs = dbu.executeQuery(sql, DATABASE);
		int sheetnum = 1;
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			Row row0 = structureWb.cloneSheet(0).getRow(0);
			structureWb.setSheetName(sheetnum, tableName);
			dataWb.createSheet(tableName);
			row0.createCell(1).setCellValue(tableName);
			row0.createCell(4).setCellValue(rs.getString("TABLE_COMMENT"));
			writeStructure(tableName);
			writeData(tableName);
			sheetnum++;
		}
		structureWb.setSheetHidden(0, true);
	}

	static void writeStructure(String tableName) throws SQLException {
		out.print("Analyzing table " + tableName + "...");
		String sql = "select ORDINAL_POSITION,COLUMN_NAME,COLUMN_TYPE,COLUMN_COMMENT,COLUMN_DEFAULT,IS_NULLABLE "
				+ " from information_schema.columns where TABLE_SCHEMA=? and TABLE_NAME=?";
		ResultSet rs = dbu.executeQuery(sql, DATABASE, tableName);
		int rownum = 2;
		while (rs.next()) {
			Row row = structureWb.getSheet(tableName).createRow(rownum);
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

	static void writeData(String tableName) throws SQLException {
		out.print("Dumping data...");
		String sql = "select * from " + tableName;
		ResultSet rs = dbu.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Sheet sheet = dataWb.getSheet(tableName);
		int rowNum = 1;
		while (rs.next()) {
			Row row = sheet.createRow(rowNum);
			for (int i = 0; i < columnCount; i++)
				row.createCell(i).setCellValue(rs.getString(i + 1));
			rowNum++;
		}
		Row row0 = sheet.createRow(0);
		for (int i = 0; i < columnCount; i++) {
			row0.createCell(i).setCellValue(rsmd.getColumnName(i + 1));
			sheet.autoSizeColumn(i);
		}
		out.println("done");
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
		ResultSet rs = dbu.executeQuery(sql, DATABASE, tableName);
		StringBuilder primaryKey = new StringBuilder();
		while (rs.next())
			primaryKey.append(rs.getString("COLUMN_NAME")).append(",");
		if (primaryKey.length() != 0)
			primaryKey.deleteCharAt(primaryKey.length() - 1);
		// TODO Write primary key info to Excel
	}

	/**
	 * Note that a unique constraint can contains more than one column, and a
	 * table may have more than one unique constraint, but different unique
	 * constraint has different names.
	 */
	static void writeUniqueKey(String tableName) throws SQLException {
		String sql = "select CONSTRAINT_NAME from information_schema.table_constraints "
				+ " where TABLE_SCHEMA=? and TABLE_NAME=? and CONSTRAINT_TYPE='UNIQUE'";
		ResultSet rs = dbu.executeQuery(sql, DATABASE, tableName);
		StringBuilder unique = new StringBuilder();
		while (rs.next())
			unique.append(
					writeUniqueKey(tableName, rs.getString("CONSTRAINT_NAME")))
					.append(",");
		if (unique.length() != 0)
			unique.deleteCharAt(unique.length() - 1);
		// TODO Write unique key info to Excel.
	}

	static String writeUniqueKey(String tableName, String constraintName)
			throws SQLException {
		String sql = "select COLUMN_NAME from information_schema.key_column_usage "
				+ " where TABLE_SCHEMA=? and TABLE_NAME=? and CONSTRAINT_NAME=?";
		ResultSet rs = dbu.executeQuery(sql, DATABASE, tableName,
				constraintName);
		rs.last();
		if (rs.getRow() == 1)
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
		ResultSet rs = dbu.executeQuery(sql, DATABASE, tableName);
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

	public static void main(String[] args) throws IOException, SQLException,
			InvalidFormatException {
		String url = "jdbc:mysql://" + IP + ":" + PORT + "/" + DATABASE;
		File structure = new File("D:/Documents/" + DATABASE + "Structure.xlsx");
		try (OutputStream structureOut = new FileOutputStream(structure);
				OutputStream dataOut = new FileOutputStream("D:/Documents/"
						+ DATABASE + "Data.xlsx")) {
			dbu = new DbUtil(url, USER, PASSWORD);
			try (InputStream in = new FileInputStream(
					"src/database/Structure.xlsx");) {
				byte[] bytes = new byte[in.available()];
				in.read(bytes);
				structureOut.write(bytes);
			}
			structureWb = create(structure);
			dataWb = new XSSFWorkbook();
			write();
			structureWb.write(structureOut);
			dataWb.write(dataOut);
		} finally {
			close(structureWb, dataWb, dbu);
		}
	}
}
