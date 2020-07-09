package elasticsearch;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import exceptions.ElasticException;
import exceptions.HttpException;
import util.Constants;
import util.HttpManager;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticServiceProvider {

	private static Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(ElasticServiceProvider.class.getSimpleName());
	private String url = prop.getProperty("elasticUrl");
	private Map<String, String> headers;
	private int timeout = Integer.parseInt(prop.getProperty("elasticTimeOut"));
	String bulkUrl;
	String elasticUrl;
	String searchUrl;

	public ElasticServiceProvider() throws ElasticException, HttpException {
		
	}

	public ElasticServiceProvider(Map<String, String> headers) throws ElasticException, HttpException {
		this.headers = headers;
	}
	
	public ElasticServiceProvider(String url, Map<String, String> headers, int timeout) throws ElasticException, HttpException {
		this.url = url;
		this.headers = headers;
		this.timeout = timeout;
	}

	public void bulkSave(String index, String type, String data) throws ElasticException, HttpException, IOException {

		String params = "/" + index + "/" + type;
		String response[] = HttpManager.httpCall(url + params + Constants.Bulk, headers, timeout, data, "POST");

		int code = Integer.parseInt(response[0]);
		JsonParser jpar = new JsonParser();
		JsonObject jobj1 = jpar.parse(response[1]).getAsJsonObject();

		String foundValue = jobj1.get("errors").getAsString();

		if (code == 200 & foundValue.equalsIgnoreCase("true")) {
			log.error("Invalid Json_Data _format");
			throw new ElasticException(code, response[1]);
		}

		if (code != 200) {
			log.error("Invalid Json_NewLine character is Missing");
			throw new ElasticException(code, response[1]);
		}
	}

	public void bulkSave(String index, String data) throws ElasticException, HttpException, IOException {
		String params = "/" + index;
		String response[] = HttpManager.httpCall(url + params + Constants.Bulk, headers, timeout, data, "POST");
		int code = Integer.parseInt(response[0]);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}
	}

	public String bulkSave(String data) throws ElasticException, HttpException, IOException {		
		log.info("\n Bulk Save has started");		
		String response[] = HttpManager.httpCall(url + Constants.Bulk, headers, timeout, data, "POST");

		int code = Integer.parseInt(response[0]);
		System.out.println("Response Code: "+code);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}
		
		return data;
	}

	public void Save(String index, String type, String data) throws ElasticException, HttpException, IOException {
		String params = "/" + index + "/" + type;
		String response[] = HttpManager.httpCall(url + params, headers, timeout, data, "POST");
		int code = Integer.parseInt(response[0]);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}
	}

	public void Save(String index, String type, String uid, String data)
			throws ElasticException, HttpException, IOException {
		String params = "/" + index + "/" + type + "/" + uid;
		String response[] = HttpManager.httpCall(url + params, headers, timeout, data, "PUT");
		int code = Integer.parseInt(response[0]);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}
	}

	public String searchByID(String index, String type, String uid)
			throws ElasticException, HttpException, IOException {
		String params = "/" + index + "/" + type + "/" + uid;
		String response[] = HttpManager.httpCall(url + params, headers, timeout, null, "GET");
		int code = Integer.parseInt(response[0]);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}

		return response[1];
	}

	public JsonArray gethits(JsonObject Globalobj) {
		return Globalobj.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
	}

	public String searchByQuery(String index, String type, String query)
			throws HttpException, ElasticException, IOException {
		String params = "/" + index + "/" + type;

		String response[] = HttpManager.httpCall(url + params + Constants.search, headers, timeout, query, "POST");
		int code = Integer.parseInt(response[0]);

		if (code != 200) {
			throw new ElasticException(code, response[1]);
		}

		return response[1];
	}

}