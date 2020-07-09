package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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

public class V1BurnDownLoader implements Loader {

	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(V1BurnDownLoader.class.getSimpleName());

	public String getV1Report() throws HttpException, IOException {

		log.info("Version One Burn Down method has started");
		String responseJson = "";
		String[] respData = new String[2];
		String jsonStr = null;
		HashMap<String, String> epicMap = new HashMap<String, String>();
		HashMap<String, String> featureMap = new HashMap<String, String>();
		LocalDateTime ldt = LocalDateTime.now();
		int timeout = Integer.parseInt(prop.getProperty(Constants.V1_TIMEOUT));
		String v1Index = prop.getProperty(Constants.V1_BURNDOWN_INDEX);
		String v1Type = prop.getProperty(Constants.V1_TYPE);
		String authType = prop.getProperty(Constants.V1_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.V1_AUTH_VALUE);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		getEpicData(epicMap, headers, timeout);
		getFeatureData(featureMap, headers, timeout);
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_BURNDOWN_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_BURNDOWN_QP)
				+ prop.getProperty(Constants.BD_FILTER_PARAM), headers, timeout, null, "GET");
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
			idxOutputJson.addProperty(Constants.ID, ldt + "_" + attrObj.get(Constants.ID_NUMBER).getAsJsonObject()
					.get(Constants.VALUE).getAsString());
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
				} else if(entry.getKey().equalsIgnoreCase(Constants.PARENT_EPIC_ID)) {
					if(entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsJsonArray().size() > 1) {
						for(int i = 0; i < entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsJsonArray().size(); i++) {
							String id = entry.getValue().getAsJsonObject().get(Constants.VALUE)
									.getAsJsonArray().get(i).toString().substring(1, 
											entry.getValue().getAsJsonObject().get(Constants.VALUE)
											.getAsJsonArray().get(i).toString().length()-1);
							if(epicMap.containsKey(id)) {
								outputJson.addProperty(Constants.EPIC_ID, id);
								outputJson.addProperty(Constants.EPIC_NAME, epicMap.get(id));
							} else if(featureMap.containsKey(id)) {
								outputJson.addProperty(Constants.FEATURE_ID, id);
								outputJson.addProperty(Constants.FEATURE_NAME, featureMap.get(id));
							}
						}
					} else {
						outputJson.addProperty(Constants.EPIC_ID, "--");
						outputJson.addProperty(Constants.EPIC_NAME, "--");
						outputJson.addProperty(Constants.FEATURE_ID, "--");
						outputJson.addProperty(Constants.FEATURE_NAME, "--");
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
		if(key.equalsIgnoreCase(Constants.PARENT_STATUS)) {
			if (value.getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
				return Constants.READY;
			} else {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
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
	
	public void getEpicData(HashMap<String, String> epicMap, HashMap<String, String> headers, int timeout) throws HttpException, IOException {
		String[] respData = new String[2];
		String jsonStr = null;
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_EPIC_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_EPIC_QP)
				+ prop.getProperty(Constants.EPIC_FILTER_PARAM), headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray workItemArray = new JsonArray();
		workItemArray = objectJO.get(Constants.ASSETS).getAsJsonArray();
		for (JsonElement element : workItemArray) {
			JsonObject detailsObj = element.getAsJsonObject();
			JsonObject attrObj = detailsObj.get(Constants.ATTRIBUTES).getAsJsonObject();
			String storyId = attrObj.get(Constants.ID_NUMBER).getAsJsonObject().get(Constants.VALUE).getAsString();
			String storyName = attrObj.get(Constants.NAME_UC).getAsJsonObject().get(Constants.VALUE).getAsString();
			epicMap.put(storyId, storyName);
		}
	}
	
	public void getFeatureData(HashMap<String, String> featureMap, HashMap<String, String> headers, int timeout) throws HttpException, IOException {
		String[] respData = new String[2];
		String jsonStr = null;
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_EPIC_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_EPIC_QP)
				+ prop.getProperty(Constants.FEATURE_FILTER_PARAM), headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray workItemArray = new JsonArray();
		workItemArray = objectJO.get(Constants.ASSETS).getAsJsonArray();
//		log.info("Work Item Array" + workItemArray);
		for (JsonElement element : workItemArray) {
			JsonObject detailsObj = element.getAsJsonObject();
			JsonObject attrObj = detailsObj.get(Constants.ATTRIBUTES).getAsJsonObject();
			String storyId = attrObj.get(Constants.ID_NUMBER).getAsJsonObject().get(Constants.VALUE).getAsString();
			String storyName = attrObj.get(Constants.NAME_UC).getAsJsonObject().get(Constants.VALUE).getAsString();
			featureMap.put(storyId, storyName);
		}
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Version One Report----->" + getV1Report());
			provider.bulkSave(getV1Report());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
