package database;

import static org.apache.poi.ss.usermodel.WorkbookFactory.create;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

class ExcelToDatabase {

	static DbUtil dbu;

	static String database;

	static Workbook structureWb;

	static Workbook dataWb;

	public static void main(String[] args) throws InvalidFormatException,
			IOException, SQLException {
		String ip = "localhost";
		int port = 3306;
		String user = "root";
		String password = "";
		database = "webit";
		String url = "jdbc:mysql://" + ip + ":" + port + "/" + database;
		File structure = new File("D:/Documents/" + database + "Structure.xlsx");
		File data = new File("D:/Documents/" + database + "Data.xlsx");
		try {
			dbu = new DbUtil(url, user, password);
			structureWb = create(structure);
			dataWb = create(data);
		} finally {

		}
	}
}
