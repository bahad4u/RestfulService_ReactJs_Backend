package loader;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import util.Constants;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KeysLoader {
	
	private static Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(KeysLoader.class.getSimpleName());
	private int timeout = Integer.parseInt(prop.getProperty(Constants.ELASTIC_TIMEOUT));
	Map<String,String> headers;
	public static int sprint;
	public static String release;
	public static ElasticServiceProvider provider;
	
	
	public KeysLoader() throws ElasticException, HttpException, IOException {
		provider = new ElasticServiceProvider(prop.getProperty(Constants.ELASTIC_URL), headers, timeout);
		this.fetchDataFromGlobalKeys();
		// this.fetchDataFromGlobalKeysByDate(); //To get global keys by Date
	}
	
	public void fetchDataFromGlobalKeys() throws ElasticException, HttpException, IOException {
		String index = prop.getProperty(Constants.GLOBAL_INDEX);
		String type = prop.getProperty(Constants.GLOBAL_TYPE);
		String query = "{\"query\":{\"bool\":{\"must\":[{\"range\":{\"startdate\":{\"lte\":\"now\"}}},{\"range\":{\"enddate\":{\"gte\":\"now\"}}}]}}}";
		System.out.println("Checking " + query);
		String response = provider.searchByQuery(index, type, query);
		JsonParser jpar = new JsonParser();
		JsonObject globalobj = jpar.parse(response).getAsJsonObject();
		JsonArray hits = provider.gethits(globalobj);
		if (hits.size() >= 1) {
			JsonObject jo1 = hits.get(0).getAsJsonObject().get(Constants.SOURCE).getAsJsonObject();
			setSprint(jo1.get(Constants.SPRINT).getAsInt());
			setRelease(jo1.get(Constants.RELEASE).getAsString());

			System.out.println("Sprint value: " + sprint);
			System.out.println("Release value: " + release);
		}
	}
	
	public void fetchDataFromGlobalKeysByDate() throws ElasticException, HttpException, IOException {
		String index = prop.getProperty(Constants.GLOBAL_INDEX);
		String type = prop.getProperty(Constants.GLOBAL_TYPE);
		String query = "{\"query\":{\"bool\":{\"must\":[{\"range\":{\"startdate\":{\"lte\":\"now\"}}},{\"range\":{\"enddate\":{\"gte\":\"now\"}}}]}}}";

		String response = provider.searchByQuery(index, type, query);
		JsonParser jpar = new JsonParser();
		JsonObject globalobj = jpar.parse(response).getAsJsonObject();
		JsonArray hits = provider.gethits(globalobj);
		JsonObject jo1 = hits.get(0).getAsJsonObject().get(Constants.SOURCE).getAsJsonObject();
		setSprint(jo1.get(Constants.SPRINT).getAsInt());
		setRelease(jo1.get(Constants.RELEASE).getAsString());

		log.info("Sprint value: " + sprint + "\n  Release value: " + release);
	}
	
	public void setSprint(int num) {
		sprint = num;
	}

	public void setRelease(String str) {
		release = str;
	}

	public static int getSprint() {
		return sprint;
	}

	public static String getRelease() {
		return release;
	}
}
