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

public class V1EpicLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(V1EpicLoader.class.getSimpleName());

	public String getV1EpicReport() throws Exception {

		log.info("Version One Epic method has started");
		String responseJson = "";
		String[] respData = new String[2];
		String jsonStr = null;
		LocalDateTime ldt = LocalDateTime.now();
		int timeout = Integer.parseInt(prop.getProperty(Constants.V1_TIMEOUT));
		String v1Index = prop.getProperty(Constants.V1_EPIC_INDEX);
		String v1Type = prop.getProperty(Constants.V1_TYPE);
		String authType = prop.getProperty(Constants.V1_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.V1_AUTH_VALUE);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_EPIC_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_EPIC_QP)
				, headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray workItemArray = new JsonArray();
		workItemArray = objectJO.get(Constants.ASSETS).getAsJsonArray();
//		log.info("Work Item Array" + workItemArray);
		for (JsonElement element : workItemArray) {
			JsonObject outputJson = new JsonObject();
			JsonObject detailsObj = element.getAsJsonObject();
			JsonObject attrObj = detailsObj.get(Constants.ATTRIBUTES).getAsJsonObject();
//			System.out.println("Business Value------->" + attrObj.get(Constants.BUSINESS_VALUE).getAsJsonObject().get(Constants.VALUE).isJsonNull());
			Set<Entry<String, JsonElement>> entries = attrObj.entrySet();
			outputJson.addProperty(Constants.DATE, ldt.toString());
			for(Entry<String, JsonElement> entry : entries) {
				if (entry.getKey().equalsIgnoreCase(Constants.SUBS_DOWN_NUMBER)) {
					responseJson += entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsJsonArray().toString();
//					responseJson += epicDetails(attrObj, entry, headers, timeout, v1Index, v1Type);
				}
			}
		}
		return responseJson;
	}
	
	
	public String epicDetails(JsonObject attrObj, Entry<String, JsonElement> storySet,
	        HashMap<String, String> headers, int timeout, String v1Index, String v1Type) throws HttpException, IOException {
		
		String subsResponse = "";
		JsonArray workItems = storySet.getValue().getAsJsonObject().get(Constants.VALUE).getAsJsonArray();
		for(JsonElement workItem : workItems) {
			String workItemId = workItem.toString().substring(1, workItem.toString().length()-1);
			if(workItemId.matches("^E.*")) {
				subsResponse += fetchSubEpicDetails(attrObj, workItemId, headers, timeout, v1Index, v1Type);
			} else if(workItemId.matches("^S.*")) {
				subsResponse += fetchSubStoryDetails(attrObj, workItemId, headers, timeout, v1Index, v1Type);
			}
		}
		return subsResponse;
	}
	
	public String fetchSubEpicDetails(JsonObject epicAttrObj, String workItemId,
	        HashMap<String, String> headers, int timeout, String v1Index,
	        String v1Type) throws HttpException, IOException {
//		System.out.println("Epic Id------>" + workItemId);
		String responseJson = "";
		LocalDateTime ldt = LocalDateTime.now();
		String[] respData = new String[2];
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_EPIC_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_EPIC_QP)
				+ prop.getProperty(Constants.EPIC_FILTER_PARAM) + workItemId + prop.getProperty("pagination"), headers, timeout, null, "GET");
		String epicId = epicAttrObj.get(Constants.ID_NUMBER).getAsJsonObject().get(Constants.VALUE).getAsString();
		String epicName = epicAttrObj.get(Constants.NAME_UC).getAsJsonObject().get(Constants.VALUE).getAsString();
		String jsonStr = respData[1];
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
			idxOutputJson.addProperty(Constants.ID, ldt.toString() + "_" + epicId);
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson = indexJo.toString() + "\n";
			Set<Entry<String, JsonElement>> entries = attrObj.entrySet();
			outputJson.addProperty(Constants.EPIC_ID, epicId);
			outputJson.addProperty(Constants.EPIC_NAME, epicName);
			outputJson.addProperty(Constants.EPIC_BUSINESS_VALUE, getEpicValues(epicAttrObj, Constants.BUSINESS_VALUE));
			outputJson.addProperty(Constants.EPIC_STATUS, getEpicValues(epicAttrObj, Constants.V1_STATUS_NAME));
			outputJson.addProperty(Constants.EPIC_TEAM, getEpicValues(epicAttrObj, Constants.TEAM_NAME));
			outputJson.addProperty(Constants.EPIC_SCOPE, getEpicValues(epicAttrObj, Constants.SCOPE_NAME));
			outputJson.addProperty(Constants.ASSET_TYPE, getEpicValues(epicAttrObj, Constants.ASSET_TYPE));
			outputJson.addProperty(Constants.EPIC_CATEGORY, getEpicValues(epicAttrObj, Constants.CATEGORY_NAME));
			outputJson.addProperty(Constants.EPIC_OWNERS, getEpicValues(epicAttrObj, Constants.OWNER));
			outputJson.addProperty(Constants.EPIC_ESTIMATE, getEpicValues(epicAttrObj, Constants.SUBS_ESTIMATE));
			outputJson.addProperty(Constants.DATE, ldt.toString());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			for(Entry<String, JsonElement> entry : entries) {
				if(entry.getKey().equalsIgnoreCase(Constants.SUBS_ESTIMATE)) {
					if (entry.getValue().getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
						outputJson.addProperty(entry.getKey(), 0);
					} else {
						outputJson.addProperty(entry.getKey(), entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsInt());
					}
				} else if (!entry.getKey().equalsIgnoreCase(Constants.SUBS_DOWN_NUMBER)) {
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
	
	public String fetchSubStoryDetails(JsonObject epicAttrObj, String workItemId,
	        HashMap<String, String> headers, int timeout, String v1Index,
	        String v1Type) throws HttpException, IOException {
//		System.out.println("WorkItem Id------>" + workItemId);
		String responseJson = "";
		LocalDateTime ldt = LocalDateTime.now();
		String[] respData = new String[2];
		respData = HttpManager.httpCall(prop.getProperty(Constants.V1_VELOCITY_URL) 
				+ prop.getProperty(Constants.ACCEPT_HEADER) + prop.getProperty(Constants.V1_STORY_QP)
				+ prop.getProperty(Constants.EPIC_FILTER_PARAM) + workItemId + prop.getProperty("pagination"), headers, timeout, null, "GET");
		String epicId = epicAttrObj.get(Constants.ID_NUMBER).getAsJsonObject().get(Constants.VALUE).getAsString();
		String epicName = epicAttrObj.get(Constants.NAME_UC).getAsJsonObject().get(Constants.VALUE).getAsString();
		String jsonStr = respData[1];
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
			idxOutputJson.addProperty(Constants.ID, ldt.toString() + "_" + epicId);
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson = indexJo.toString() + "\n";
			Set<Entry<String, JsonElement>> entries = attrObj.entrySet();
			outputJson.addProperty(Constants.EPIC_ID, epicId);
			outputJson.addProperty(Constants.EPIC_NAME, epicName);
			outputJson.addProperty(Constants.EPIC_BUSINESS_VALUE, getEpicValues(epicAttrObj, Constants.BUSINESS_VALUE));
			outputJson.addProperty(Constants.EPIC_STATUS, getEpicValues(epicAttrObj, Constants.V1_STATUS_NAME));
			outputJson.addProperty(Constants.EPIC_TEAM, getEpicValues(epicAttrObj, Constants.TEAM_NAME));
			outputJson.addProperty(Constants.EPIC_SCOPE, getEpicValues(epicAttrObj, Constants.SCOPE_NAME));
			outputJson.addProperty(Constants.ASSET_TYPE, getEpicValues(epicAttrObj, Constants.ASSET_TYPE));
			outputJson.addProperty(Constants.EPIC_CATEGORY, getEpicValues(epicAttrObj, Constants.CATEGORY_NAME));
			outputJson.addProperty(Constants.EPIC_OWNERS, getEpicValues(epicAttrObj, Constants.OWNER));
			outputJson.addProperty(Constants.EPIC_ESTIMATE, getEpicValues(epicAttrObj, Constants.SUBS_ESTIMATE));
			outputJson.addProperty(Constants.CATEGORY_NAME, Constants.STORY);
			outputJson.addProperty(Constants.DATE, ldt.toString());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			for(Entry<String, JsonElement> entry : entries) {
				if (entry.getKey().equalsIgnoreCase(Constants.ESTIMATE)
				        || entry.getKey().equalsIgnoreCase(Constants.CHILD_TOTAL_HRS)
				        || entry.getKey().equalsIgnoreCase(Constants.CHILD_CMPLTD_HRS)
				        || entry.getKey().equalsIgnoreCase(Constants.CHILD_TODO_HRS)) {
					if (entry.getValue().getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
						outputJson.addProperty(entry.getKey(), 0);
					} else {
						outputJson.addProperty(entry.getKey(), entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsInt());
					}
				} else {
					outputJson.addProperty(entry.getKey(), getValue(entry.getKey(), entry.getValue()));
				}
			}
			if(outputJson.get(Constants.V1_STATUS_NAME).getAsString().equalsIgnoreCase(Constants.DONE)) {
				outputJson.addProperty(Constants.COMMITTED, outputJson.get(Constants.ESTIMATE).getAsInt());
				outputJson.addProperty(Constants.DELIVERED, outputJson.get(Constants.ESTIMATE).getAsInt());
			} else {
				outputJson.addProperty(Constants.COMMITTED, outputJson.get(Constants.ESTIMATE).getAsInt());
				outputJson.addProperty(Constants.DELIVERED, 0);
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
				return "--";
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
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				if (value.getAsJsonObject().get(Constants.VALUE).getAsInt() == 64) {
					return Constants.ACTIVE;
				} else if (value.getAsJsonObject().get(Constants.VALUE).getAsInt() == 0) {
					return Constants.FUTURE;
				} else {
					return Constants.CLOSED; //Value of Closed State is 128.
				}
			} else {
				return "--";
			}
		} else if(key.equalsIgnoreCase(Constants.TIMEBOX_NAME)) {
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString().split("-")[0].trim();
			} else {
				return "--";
			}
		} else if (key.equalsIgnoreCase(Constants.OWNER)) {
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return value.getAsJsonObject().get(Constants.VALUE).getAsJsonArray().toString();
			} else {
				return Constants.NIL;
			}
		} else if (key.equalsIgnoreCase(Constants.BUSINESS_VALUE)) {
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
			} else {
				return "--";
			}
		} else if (key.equalsIgnoreCase(Constants.CATEGORY_NAME)) {
			if (!(value.getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return value.getAsJsonObject().get(Constants.VALUE).getAsString();
			} else {
				return "--";
			}
		} else {
//			System.out.println("Entry Set Values----->" + key + "--" + value.getAsJsonObject().get(Constants.VALUE));	
			return value.getAsJsonObject().get(Constants.VALUE).getAsString();
		}
	}
	
	public String getEpicValues(JsonObject epicObj, String identifier) {
//		System.out.println("Key Value Check---->" + identifier + "--" 
//				+ epicObj.get(identifier).getAsJsonObject().get(Constants.VALUE));
		if (identifier.equalsIgnoreCase(Constants.OWNER)) {
			if (!(epicObj.get(identifier).getAsJsonObject().get(Constants.VALUE).isJsonNull())) {
				return epicObj.get(identifier).getAsJsonObject().get(Constants.VALUE).getAsJsonArray().toString();
			} else {
				return "--";
			}
		} else {
			return epicObj.get(identifier).getAsJsonObject().get(Constants.VALUE).isJsonNull() 
				? "--" : epicObj.get(identifier).getAsJsonObject().get(Constants.VALUE).getAsString();
		}
	}
		
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Version One Report----->" + getV1EpicReport());
			provider.bulkSave(getV1EpicReport());
		}catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		}catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
