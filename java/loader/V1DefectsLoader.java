package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

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

public class V1DefectsLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(V1DefectsLoader.class.getSimpleName());

	public String getV1DefectsReport() throws HttpException, IOException {

		log.info("Version One Defects method has started");
		String responseJson = "";
		String[] respData = new String[2];
		String jsonStr = null;
		LocalDateTime ldt = LocalDateTime.now();
		int timeout = Integer.parseInt(prop.getProperty(Constants.V1_TIMEOUT));
		String v1Index = prop.getProperty(Constants.V1_DEFECTS_INDEX);
		String v1Type = prop.getProperty(Constants.V1_TYPE);
		String authType = prop.getProperty(Constants.V1_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.V1_AUTH_VALUE);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_DEFECTS_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_DEFECTS_QP)
				+ prop.getProperty(Constants.DEFECTS_FILTER_PARAM), headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray workItemArray = new JsonArray();
		workItemArray = objectJO.get(Constants.ASSETS).getAsJsonArray();
//		log.info("Work Item Array" + workItemArray);
		for (JsonElement element : workItemArray) {
			JsonObject idxOutputJson = new JsonObject();
			JsonObject indexJo = new JsonObject();
			JsonObject outputJson = new JsonObject();
			JsonObject detailsObj = element.getAsJsonObject();
			JsonObject attrObj = detailsObj.get(Constants.ATTRIBUTES).getAsJsonObject();
			idxOutputJson.addProperty(Constants.INDEX, v1Index);
			idxOutputJson.addProperty(Constants.TYPE, v1Type);
			idxOutputJson.addProperty(Constants.ID, attrObj.get(Constants.TIMEBOX_NAME).getAsJsonObject()
					.get(Constants.VALUE).getAsString().split("-")[0].trim() + "_" + attrObj.get(Constants.ID_NUMBER)
					.getAsJsonObject().get(Constants.VALUE).getAsString());
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson += indexJo.toString() + "\n";
			Set<Entry<String, JsonElement>> entries = attrObj.entrySet();
			outputJson.addProperty(Constants.DATE, ldt.toString());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			for(Entry<String, JsonElement> entry : entries) {
				if(entry.getKey().equalsIgnoreCase(Constants.PARENT_ESTIMATE) || entry.getKey().equalsIgnoreCase(Constants.DETAIL_ESTIMATE)
						|| entry.getKey().equalsIgnoreCase(Constants.TO_DO) || entry.getKey().equalsIgnoreCase(Constants.ACTUALS)) {
					if (entry.getValue().getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
						outputJson.addProperty(entry.getKey(), 0);
					} else {
						outputJson.addProperty(entry.getKey(), entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsInt());
					}
				} else {
					outputJson.addProperty(entry.getKey(), getValue(entry.getKey(), entry.getValue()));
				}
			}
			if(outputJson.get(Constants.TEAM_NAME).getAsString().equalsIgnoreCase("null")) {
				outputJson.addProperty(Constants.TEAM_NAME, outputJson.get(Constants.SCOPE_NAME).getAsString());
			}
			responseJson += outputJson.toString() + "\n";
		}
		return responseJson;
	}
	
	public String getValue(String key, JsonElement value) {
		if(key.equalsIgnoreCase(Constants.V1_STATUS_NAME)) {
			if (value.getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
				return Constants.READY;
			} else {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
			}
		} else if (key.equalsIgnoreCase(Constants.SEVERITY)) { 
			if (value.getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
				return Constants.NIL;
			} else {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
			}
		} else if (key.equalsIgnoreCase(Constants.OWNER)) { 
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return value.getAsJsonObject().get(Constants.VALUE).getAsJsonArray().toString();
			} else {
				return Constants.NIL;
			}
		} else if(key.equalsIgnoreCase(Constants.TEAM_NAME)) {
			if (value.getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
				return "null";
			} else {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
			}
		} else if(key.equalsIgnoreCase(Constants.TIMEBOX_STATE)) {
			if (value.getAsJsonObject().get(Constants.VALUE).getAsInt() == 64) {
				return Constants.ACTIVE;
			} else if (value.getAsJsonObject().get(Constants.VALUE).getAsInt() == 0) {
				return Constants.FUTURE;
			} else {
				return Constants.CLOSED; //Value of Closed State is 128.
			}
		} else if(key.equalsIgnoreCase(Constants.TIMEBOX_NAME)) {
			return value.getAsJsonObject().get(Constants.VALUE).getAsString().split("-")[0].trim();
		} else {
			return value.getAsJsonObject().get(Constants.VALUE).getAsString();
		}
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Version One Report----->" + getV1DefectsReport());
			provider.bulkSave(getV1DefectsReport());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}
