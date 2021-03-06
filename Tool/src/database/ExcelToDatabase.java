package database;

import static database.Utils.close;
import static java.lang.System.out;
import static java.util.stream.Stream.of;
import static org.apache.poi.ss.usermodel.WorkbookFactory.create;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Finally
 * @since 1.8
 */
class ExcelToDatabase {

	static final String IP = "localhost";
	static final int PORT = 3306;
	static final String USER = "root";
	static final String PASSWORD = "";
	static final String DATABASE = "parkdb";

	static DbUtil dbu;

	static Workbook structureWb;

	static Workbook dataWb;

	static void read() throws SQLException {
		for (int i = 1; i < structureWb.getNumberOfSheets(); i++) {
			String tableName = structureWb.getSheetName(i);
			readStructure(tableName);
			readData(tableName);
		}
	}

	static void readStructure(String tableName) throws SQLException {
		dbu.execute("drop table if exists `" + tableName + "`");
		Sheet sheet = structureWb.getSheet(tableName);
		int rowNum = 3;
		StringBuilder sql = new StringBuilder("create table `").append(
				tableName).append("` (");
		Row row = sheet.getRow(rowNum);
		do {
			sql.append("`").append(row.getCell(1).getStringCellValue())
					.append("` ").append(row.getCell(2).getStringCellValue());
			if ("NO".equalsIgnoreCase(row.getCell(4).getStringCellValue()))
				sql.append(" NOT NULL ");
			Cell cellDefault = row.getCell(5);
			if (cellDefault != null) {
				sql.append(" DEFAULT ");
				String dftValue = cellDefault.getStringCellValue();
				if (!dftValue.trim().isEmpty())
					sql.append(dftValue);
			}
			Cell commentCell = row.getCell(3);
			if (commentCell != null
					&& !commentCell.getStringCellValue().isEmpty())
				sql.append(" comment '")
						.append(commentCell.getStringCellValue()).append("'");
			sql.append(",");
			rowNum++;
			row = sheet.getRow(rowNum);
		} while (row != null);
		row = sheet.getRow(1);
		Cell primaryKeyCell = row.getCell(1);
		if (primaryKeyCell != null
				&& !primaryKeyCell.getStringCellValue().isEmpty())
			sql.append("primary key (").append(primaryKeyCell).append("),");
		Cell uniqueKeyCell = row.getCell(4);
		if (uniqueKeyCell != null) {
			String unique = uniqueKeyCell.getStringCellValue();
			if (!unique.isEmpty()) {
				String[] uniques = unique.split(";");
				of(uniques).forEach(
						u -> sql.append("unique key (").append(u).append("),"));
			}
		}
		sql.deleteCharAt(sql.length() - 1).append(")");
		Cell tableCommentCell = sheet.getRow(0).getCell(4);
		if (tableCommentCell != null
				&& !tableCommentCell.getStringCellValue().isEmpty())
			sql.append(" comment '")
					.append(tableCommentCell.getStringCellValue()).append("'");
		out.println(sql);
		dbu.execute(sql.toString());
	}

	static void readData(String tableName) throws SQLException {
		Sheet sheet = dataWb.getSheet(tableName);
		if (sheet.getRow(1) == null)
			return;
		Row row = sheet.getRow(0);
		StringBuilder sql = new StringBuilder("insert into ").append(tableName)
				.append(" (");
		int columnCount = 0;
		Cell cell = row.getCell(columnCount);
		do {
			sql.append(cell.getStringCellValue()).append(",");
			columnCount++;
			cell = row.getCell(columnCount);
		} while (cell != null);
		sql.deleteCharAt(sql.length() - 1).append(") values ");
		List<Object> params = new ArrayList<>();
		int rowNum = 1;
		row = sheet.getRow(rowNum);
		do {
			sql.append("(");
			for (int i = 0; i < columnCount; i++) {
				sql.append("?,");
				cell = row.getCell(i);
				params.add(cell == null ? null : cell.getStringCellValue());
			}
			sql.deleteCharAt(sql.length() - 1).append("),");
			rowNum++;
			row = sheet.getRow(rowNum);
		} while (row != null);
		sql.deleteCharAt(sql.length() - 1);
		dbu.executeUpdate(sql.toString(), params.toArray());
		out.println("Query OK, " + (rowNum - 1) + " rows affected");
	}

	public static void main(String[] args) throws InvalidFormatException,
			IOException, SQLException {
		String url = "jdbc:mysql://" + IP + ":" + PORT + "/" + DATABASE;
		File structure = new File("D:/Documents/" + DATABASE + "Structure.xlsx");
		File data = new File("D:/Documents/" + DATABASE + "Data.xlsx");
		try {
			dbu = new DbUtil(url, USER, PASSWORD);
			structureWb = create(structure);
			dataWb = create(data);
			read();
		} finally {
			close(dbu, structureWb, dataWb);
		}
	}
}
