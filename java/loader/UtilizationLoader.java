package loader;

import java.io.File;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.ExcelReader;
import util.HttpManager;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UtilizationLoader implements Loader {

	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log =
	    LogManager.getLogger(UtilizationLoader.class.getSimpleName());

	public String getUtilizationReport() throws Exception {

		log.info("Version One Utilization method has started");
		String responseJson = "";
		String[] respData = new String[2];
		String[] taskData = new String[2];
		String jsonStr = null;
		String taskJson = null;
		LocalDateTime ldt = LocalDateTime.now();
		ExcelReader reader = new ExcelReader();
		Map<String, String> associateMap = reader.loadExcelLines(new File(prop.getProperty(Constants.V1_UTIL_NAMELIST)));
		int timeout = Integer.parseInt(prop.getProperty(Constants.V1_TIMEOUT));
		String v1Index = prop.getProperty(Constants.V1_UTIL_INDEX);
		String v1Type = prop.getProperty(Constants.V1_TYPE);
		String authType = prop.getProperty(Constants.V1_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.V1_AUTH_VALUE);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		respData =
		    HttpManager
		            .httpCall(
		                    prop.getProperty(Constants.V1_UTIL_URL)
		                            + prop.getProperty(Constants.ACCEPT_HEADER)
		                            + prop.getProperty(Constants.V1_UTIL_QP)
		                            + prop.getProperty(
		                                    Constants.V1_UTIL_FILTER_PARAM),
		                    headers, timeout, null, "GET");
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
			JsonObject attrObj =
			    detailsObj.get(Constants.ATTRIBUTES).getAsJsonObject();
			if (!(attrObj.get(Constants.V1_CAPACITY).getAsJsonObject().get(Constants.VALUE).getAsInt() == 0)) {
				String memName = Normalizer.normalize(attrObj.get(Constants.MEMBER).getAsJsonObject()
				            		.get(Constants.VALUE).getAsString(), Normalizer.Form.NFD)
	                    			.replaceAll("\\p{InCombiningDiacriticalMarks}+", "_");
				if (memName.contains("_")) {
					int idx = memName.indexOf("_");
					String firstName = memName.substring(0, idx-1);
					String lastName = memName.substring(idx+2, memName.length());
					memName = firstName + " " + lastName;
				}
				int[] capacity = {0};
				int[] totalEstimate = {0};
				String teamName = attrObj.get(Constants.TEAM_NAME).getAsJsonObject()
			            			.get(Constants.VALUE).getAsString();
				if (associateMap.containsKey(memName)) {
					Set<Entry<String, JsonElement>> entries = attrObj.entrySet();
					idxOutputJson.addProperty(Constants.INDEX, v1Index);
					idxOutputJson.addProperty(Constants.TYPE, v1Type);
					idxOutputJson.addProperty(Constants.ID, memName + "_"
					        	+ attrObj.get(Constants.TIMEBOX_NAME)
					                .getAsJsonObject().get(Constants.VALUE)
					                .getAsString().split("-")[0].trim());
					indexJo.add(Constants.DATA_INDEX, idxOutputJson);
					responseJson += indexJo.toString() + "\n";
					outputJson.addProperty(Constants.DATE, ldt.toString());
					if (KeysLoader.getRelease() != null) {
						outputJson.addProperty(Constants.RELEASE,
						        KeysLoader.getRelease());
					}
					if(associateMap.get(memName).equalsIgnoreCase(Constants.OFFSHORE)) {
						outputJson.addProperty(Constants.OFFSHORE, Constants.TRUE);
					} else {
						outputJson.addProperty(Constants.ONSITE, Constants.TRUE);
					}
					taskData =
					    HttpManager.httpCall(
					            prop.getProperty(Constants.V1_BURNDOWN_URL)
					                    + prop.getProperty(Constants.ACCEPT_HEADER)
					                    + prop.getProperty(Constants.V1_TASK_QP)
					                    + prop.getProperty(
					                            Constants.V1_TASK_FILTER_PARAM)
										+ URLEncoder.encode(memName, "UTF-8")
					                    + prop.getProperty(Constants.QUOTES)
					                    + prop.getProperty(Constants.V1_TASK_FILTER_PARAM1)
					                    + URLEncoder.encode(teamName, "UTF-8")
					                    + prop.getProperty(Constants.QUOTES),
					            headers, timeout, null, "GET");
					taskJson = taskData[1];
					JsonObject taskJO = parser.parse(taskJson).getAsJsonObject();
					JsonArray taskArray = new JsonArray();
					taskArray = taskJO.get(Constants.ASSETS).getAsJsonArray();
	//				log.info("Work Item Array" + taskArray);
					for (JsonElement task : taskArray) {
						JsonObject taskObj = task.getAsJsonObject();
						JsonObject taskAttrs =
						    taskObj.get(Constants.ATTRIBUTES).getAsJsonObject();
						Set<Entry<String, JsonElement>> taskEntries =
						    taskAttrs.entrySet();
						taskEntries.parallelStream().forEach((taskEntry) -> {
							if (taskEntry.getKey()
							        .equalsIgnoreCase(Constants.DETAIL_ESTIMATE)
							) {
								if (!taskEntry.getValue().getAsJsonObject()
								        .get(Constants.VALUE).isJsonNull()) {
									totalEstimate[0] = totalEstimate[0] + taskEntry.getValue().getAsJsonObject()
														.get(Constants.VALUE).getAsInt();
								}
							}
						});
					}
					entries.parallelStream().forEach((entry) -> {
						if (entry.getKey()
						        .equalsIgnoreCase(Constants.V1_CAPACITY)) {
							if (!entry.getValue().getAsJsonObject()
							        .get(Constants.VALUE).isJsonNull()) {
								capacity[0] = entry.getValue().getAsJsonObject().get(Constants.VALUE).getAsInt();
							}
						}
						outputJson.addProperty(entry.getKey(),
						        getValue(entry.getKey(), entry.getValue()));
					});
					outputJson.addProperty("DetailEstimate", totalEstimate[0]);
					if (capacity[0] > 0) {
						if (associateMap.get(memName).equalsIgnoreCase(Constants.OFFSHORE)) {
							outputJson.addProperty("Utilization_Off", (totalEstimate[0]*100/capacity[0]));
						} else if (associateMap.get(memName).equalsIgnoreCase(Constants.ONSITE)) {
							outputJson.addProperty("Utilization_On", (totalEstimate[0]*100/capacity[0]));
						}
						
					}
					responseJson += outputJson.toString() + "\n";
				}
			}
		}
		return responseJson;
	}

	public String getValue(String key, JsonElement value) {
		if (key.equalsIgnoreCase(Constants.TEAM_NAME)) {
			if (value.getAsJsonObject().get(Constants.VALUE).isJsonNull()) {
				return "null";
			} else {
				return value.getAsJsonObject().get(Constants.VALUE)
				        .getAsString();
			}
		} else if (key.equalsIgnoreCase(Constants.TIMEBOX_NAME)) {
			return value.getAsJsonObject().get(Constants.VALUE).getAsString()
			        .split("-")[0].trim();
		} else {
			return value.getAsJsonObject().get(Constants.VALUE).getAsString();
		}
	}

	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Version One Report----->" + getUtilizationReport());
			 provider.bulkSave(getUtilizationReport());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(), esexp.getMessage(),
			        esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),
			        httpex.getMessage(), httpex.getCause());
		} catch (Exception ex) {
			 throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}
