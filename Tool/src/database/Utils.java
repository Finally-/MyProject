package database;

import static java.util.stream.Stream.of;

class Utils {

	static void close(AutoCloseable... resources) {
		of(resources).filter((r) -> r != null).forEach((r) -> {
			try {
				r.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
