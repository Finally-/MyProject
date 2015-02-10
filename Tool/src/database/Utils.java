package database;

class Utils {

	static void close(AutoCloseable... resources) {
		for (AutoCloseable resource : resources)
			if (resource != null)
				try {
					resource.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
}
