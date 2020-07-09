package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.HttpManager;
import util.JIRAUtil;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JIRAVelocityLoader implements Loader {
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(JIRAVelocityLoader.class.getSimpleName());
	JIRAUtil util = new JIRAUtil();
	
	private String getJIRAMetrics() throws Exception {

		log.info("JIRA Report method has started");
		String finalData = "";
		String responseJson = "";
		HashMap<String, String> boards = new HashMap<String, String>();
		HashMap<String, String> sprintId = new HashMap<String, String>();
		int timeout = Integer.parseInt(prop.getProperty(Constants.JIRA_TIMEOUT));
		String jiraIndex = prop.getProperty(Constants.JIRA_MERTICS_INDEX);
		String jiraType = prop.getProperty(Constants.JIRA_TYPE);
		String authType = prop.getProperty(Constants.JIRA_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.JIRA_AUTH_VALUE);
		String mediaType = prop.getProperty(Constants.JIRA_ACCEPT_TYPE);
		String produces = prop.getProperty(Constants.JIRA_FORMAT_TYPE);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		headers.put(mediaType, produces);
		boards = util.getboards(headers, timeout);

		for (Entry<String, String> rapidList : boards.entrySet()) {
			sprintId = util.getSprintId(headers, timeout, rapidList.getKey());
			for (Entry<String, String> sprint : sprintId.entrySet()) {
				responseJson = getData(headers, timeout, jiraIndex, jiraType, rapidList, sprint);
				finalData += responseJson;
			}
		}

		return finalData;

	}

	private String getData(HashMap<String, String> headers, int timeout, String jiraIndex, String jiraType,
			Entry<String, String> rapidId, Entry<String, String> sprintId) throws HttpException, ElasticException, IOException {
		String jsonStr = null;
		String responseJson = "";
		String[] respData = new String[2];
		LocalDateTime ldt = LocalDateTime.now();
		
		respData = HttpManager.httpCall(
				prop.getProperty(Constants.JIRA_SPRINT_DATA) + rapidId.getKey()
				+ prop.getProperty(Constants.JIRA_QUERY_PARAM) + sprintId.getKey(), headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonObject workItemJo = new JsonObject();
		workItemJo = objectJO.get(Constants.CONTENTS).getAsJsonObject();
		JsonArray completedArray = new JsonArray();
		JsonArray openIssuesArray = new JsonArray();
		completedArray = workItemJo.get(Constants.COMPLETED_ISSUES).getAsJsonArray();
		openIssuesArray = workItemJo.get(Constants.INCOMPLETE_ISSUES).getAsJsonArray();

		for (JsonElement element : completedArray) {
			JsonObject idxOutputJson = new JsonObject();
			JsonObject outputJson = new JsonObject();
			JsonObject indexJo = new JsonObject();
			JsonObject stats = new JsonObject();
			JsonObject closedJO = new JsonObject();
			closedJO = element.getAsJsonObject();
			idxOutputJson.addProperty(Constants.INDEX, jiraIndex);
			idxOutputJson.addProperty(Constants.TYPE, jiraType);
			idxOutputJson.addProperty(Constants.ID, sprintId.getValue() + "_" + closedJO.get(Constants.KEY).getAsString());
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson += indexJo.toString() + "\n";
			stats = closedJO.get(Constants.CURRENT_ESTIMATE).getAsJsonObject();
			outputJson.addProperty(Constants.DATE, ldt.toString());
			outputJson.addProperty(Constants.AREA_PATH, rapidId.getValue());
			outputJson.addProperty(Constants.ITERATION_PATH, sprintId.getValue());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			outputJson.addProperty(Constants.STATE, closedJO.get(Constants.STATUS_NAME).getAsString());
			outputJson.addProperty(Constants.COMMITTED, 1);
			outputJson.addProperty(Constants.DELIVERED, 1);
			outputJson.addProperty(Constants.STORY_POINTS,
					stats.get(Constants.STAT_FIELD).getAsJsonObject().get(Constants.VALUE).getAsInt());
			outputJson.addProperty(Constants.WORKITEM_ID, closedJO.get(Constants.KEY).getAsString());
			outputJson.addProperty(Constants.WORKITEM_TYPE, closedJO.get(Constants.TYPE_NAME).getAsString());
			responseJson += outputJson.toString() + "\n";
		}

		for (JsonElement element : openIssuesArray) {
			JsonObject idxOutputJson = new JsonObject();
			JsonObject outputJson = new JsonObject();
			JsonObject indexJo = new JsonObject();
			JsonObject stats = new JsonObject();
			JsonObject openJO = new JsonObject();
			openJO = element.getAsJsonObject();
			idxOutputJson.addProperty(Constants.INDEX, jiraIndex);
			idxOutputJson.addProperty(Constants.TYPE, jiraType);
			idxOutputJson.addProperty(Constants.ID, sprintId.getValue() + "_" + openJO.get(Constants.KEY).getAsString());
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson += indexJo.toString() + "\n";
			stats = openJO.get(Constants.CURRENT_ESTIMATE).getAsJsonObject();
			outputJson.addProperty(Constants.DATE, ldt.toString());
			outputJson.addProperty(Constants.AREA_PATH, rapidId.getValue());
			outputJson.addProperty(Constants.ITERATION_PATH, sprintId.getValue());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			outputJson.addProperty(Constants.STATE, openJO.get(Constants.STATUS_NAME).getAsString());
			outputJson.addProperty(Constants.COMMITTED, 1);
			outputJson.addProperty(Constants.DELIVERED, 0);
			outputJson.addProperty(Constants.STORY_POINTS,
					stats.get(Constants.STAT_FIELD).getAsJsonObject().get(Constants.VALUE).getAsInt());
			outputJson.addProperty(Constants.WORKITEM_ID, openJO.get(Constants.KEY).getAsString());
			outputJson.addProperty(Constants.WORKITEM_TYPE, openJO.get(Constants.TYPE_NAME).getAsString());
			responseJson += outputJson.toString() + "\n";
		}
		return responseJson;
	}

	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("JIRA Metrics----->" + getJIRAMetrics());
			provider.bulkSave(getJIRAMetrics());
		}catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		}catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
