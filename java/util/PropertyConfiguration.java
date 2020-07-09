package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyConfiguration {

	private static Properties prop;
	private static Logger log = LogManager.getLogger(PropertyConfiguration.class.getSimpleName());

	static {
		prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("project.properties");
			prop.load(input);
		} catch (Exception ex) {
			log.error("Exception occured while loading ADMD properties", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					log.error("Exception occured while closing the properties stream", ex);
				}
			}
		}
	}

	public static Properties getProperties() {
		return prop;
	}

}
