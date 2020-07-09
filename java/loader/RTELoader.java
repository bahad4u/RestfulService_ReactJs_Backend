package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.HttpManager;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RTELoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(RTELoader.class.getSimpleName());

	public String getRTEReport() throws Exception {
		
		log.info("APP RTE Report Data method has started");
		String httpUrl = getItcUrl();
		String jsonStr = null;
		String responseJson = "";
		String[] respData = new String[2];
		int timeout = Integer.parseInt(prop.getProperty(Constants.RTE_TIMEOUT));
		String itcIndex = prop.getProperty(Constants.RTE_INDEX);
		String itcType = prop.getProperty(Constants.RTE_TYPE);
		LocalDateTime ldt = LocalDateTime.now();
		respData = HttpManager.httpCall(httpUrl + prop.getProperty(Constants.RTE_PART_URL) 
				+ prop.getProperty(Constants.RTE_QUERY_PARAM), null, timeout, null, "GET");
		jsonStr = respData[1];
		JsonObject idxOutputJson = new JsonObject();
		JsonObject outputJson = new JsonObject();
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		idxOutputJson.addProperty(Constants.INDEX, itcIndex);
		idxOutputJson.addProperty(Constants.TYPE, itcType);
		idxOutputJson.addProperty(Constants.ID,
				UUID.randomUUID().toString().replaceAll("-", ""));
		JsonObject indexJo = new JsonObject();
		indexJo.add(Constants.DATA_INDEX, idxOutputJson);
		responseJson += indexJo.toString() + "\n";
		outputJson.addProperty(Constants.PASS_COUNT, objectJO.get(Constants.PASS_COUNT).getAsNumber());
		outputJson.addProperty(Constants.FAIL_COUNT, objectJO.get(Constants.FAIL_COUNT).getAsNumber());
		outputJson.addProperty(Constants.SKIP_COUNT, objectJO.get(Constants.SKIP_COUNT).getAsNumber());
		outputJson.addProperty(Constants.DATE, ldt.toString());
		responseJson += outputJson.toString() + "\n";
		JsonArray oneProject = new JsonArray();
		oneProject = objectJO.get(Constants.SUITES).getAsJsonArray();
		
		for(JsonElement element : oneProject) {
			JsonArray parserArray = new JsonArray();
			parserArray = element.getAsJsonObject().get(Constants.CASES).getAsJsonArray();
			for(JsonElement jElem : parserArray) {
				JsonObject opJson = new JsonObject();
				JsonObject indexOpJson = new JsonObject();
				JsonObject indexOpJo = new JsonObject();
				JsonObject jo = jElem.getAsJsonObject();
				indexOpJson.addProperty(Constants.INDEX, itcIndex);
				indexOpJson.addProperty(Constants.TYPE, itcType);
				indexOpJson.addProperty(Constants.ID,
						UUID.randomUUID().toString().replaceAll("-", ""));
				indexOpJo.add(Constants.DATA_INDEX, indexOpJson);
				responseJson += indexOpJo.toString() + "\n";
				opJson.addProperty(Constants.CLASS_NAME, jo.get(Constants.CLASS_NAME).getAsString());
				opJson.addProperty(Constants.TEST_NAME, jo.get(Constants.NAME).getAsString());
				opJson.addProperty(Constants.STATUS, getStatus(jo.get(Constants.STATUS).getAsString()));
				opJson.addProperty(Constants.DATE, ldt.toString());
				responseJson += opJson.toString() + "\n";
			}
		}
		return responseJson;

	}

	public String getStatus(String status) {
		if (status.equalsIgnoreCase(Constants.FIXED)) {
			return Constants.PASSED;
		} else if (status.equalsIgnoreCase(Constants.REGRESSION)) {
			return Constants.FAILED;
		} else {
			return status;
		}

	}
	
	public String getItcUrl() throws HttpException, ElasticException, IOException {
		String itcUrl = null;
		log.info("APP RTE Build Url method has started");
		String jsonStr = null;
		String[] respData = new String[2];
		int timeout = Integer.parseInt(prop.getProperty(Constants.RTE_TIMEOUT));
		respData = HttpManager.httpCall(prop.getProperty(Constants.RTE_BUILD_URL) 
				+ prop.getProperty(Constants.RTE_BUILD_QP), null, timeout, null, "GET");
		jsonStr = respData[1];
		
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray oneProject = new JsonArray();
		oneProject = objectJO.get(Constants.JOBS).getAsJsonArray();
		
		for(JsonElement element : oneProject) {
			if (element.getAsJsonObject().get(Constants.NAME).getAsString().equalsIgnoreCase(Constants.LA_APP_REGRESSION)) {
				itcUrl = getBuildNumber(element);
			}
		}
		return itcUrl;
	}
	
	public String getBuildNumber(JsonElement element) {
		String itcUrl = element.getAsJsonObject().get(Constants.URL).getAsString();
		JsonObject buildObj = new JsonObject();
		buildObj = element.getAsJsonObject().get(Constants.LAST_BUILD).getAsJsonObject();
		String buildNumber = buildObj.get(Constants.NUMBER).getAsString();
		String url = itcUrl + buildNumber;
//		log.info("LA_App-Regression Itc Url" + url);
		return url;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("RTE Test Report----->" + getRTEReport());
			provider.bulkSave(getRTEReport());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}
