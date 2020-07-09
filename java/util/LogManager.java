package util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogManager {

	static {
		PropertyConfigurator.configure("log4j.properties");
	}

	public static Logger getLogger(String className) {
		return Logger.getLogger(className);
	}
}
