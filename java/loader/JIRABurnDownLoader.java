package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

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

public class JIRABurnDownLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(JIRABurnDownLoader.class.getSimpleName());
	JIRAUtil util = new JIRAUtil();

	public String getJiraReport() throws HttpException, ElasticException, IOException {

		log.info("JIRA Stats method has started");
		String responseJson = "";
		HashMap<String, String> boards = new HashMap<String, String>();
		HashMap<String, String> sprintId = new HashMap<String, String>();
		int timeout = Integer.parseInt(prop.getProperty(Constants.JIRA_TIMEOUT));
		String jiraIndex = prop.getProperty(Constants.JIRA_REPORT_INDEX);
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
				responseJson = getReport(headers, timeout, jiraIndex, jiraType,
						rapidList, sprint);
			}
		}
		return responseJson;
	}
	
	public String getReport(HashMap<String, String> headers, int timeout, String jiraIndex, String jiraType,
			Entry<String, String> rapidId, Entry<String, String> sprintId) throws HttpException, ElasticException, IOException {
		
		String jsonStr = null;
		String responseJson = "";
		String[] respData = new String[2];
		LocalDateTime ldt = LocalDateTime.now();
		respData = HttpManager.httpCall(prop.getProperty(Constants.JIRA_BURNDOWN_DATA) + rapidId.getKey() 
					  + prop.getProperty(Constants.SPRINT) + sprintId.getKey() + prop.getProperty(Constants.ISSUE)
					  + prop.getProperty(Constants.JIRA_BURNDOWN_QP), headers, timeout, null, "GET");
		jsonStr = respData[1];
		JsonParser parser = new JsonParser();
		JsonObject objectJO = parser.parse(jsonStr).getAsJsonObject();
		JsonArray issuesArray = objectJO.get(Constants.ISSUES).getAsJsonArray();
		for (JsonElement element : issuesArray) {
			JsonObject idxOutputJson = new JsonObject();
			JsonObject indexJo = new JsonObject();
			JsonObject outputJson = new JsonObject();
			JsonObject tasksObj = element.getAsJsonObject();
			JsonObject fields = tasksObj.get(Constants.FIELDS).getAsJsonObject();
			idxOutputJson.addProperty(Constants.INDEX, jiraIndex);
			idxOutputJson.addProperty(Constants.TYPE, jiraType);
			idxOutputJson.addProperty(Constants.ID, ldt.toString() + "_" + tasksObj.get(Constants.KEY).getAsString());
			indexJo.add(Constants.DATA_INDEX, idxOutputJson);
			responseJson += indexJo.toString() + "\n";
			outputJson.addProperty(Constants.DATE, ldt.toString());
			outputJson.addProperty(Constants.LAST_UPDATED, fields.get(Constants.UPDATED).getAsString());
			outputJson.addProperty(Constants.WORKITEM_ID, tasksObj.get(Constants.KEY).getAsString());
			outputJson.addProperty(Constants.WORKITEM_TYPE, fields.get(Constants.ISSUE_TYPE).getAsJsonObject().get(Constants.NAME).getAsString());
			outputJson.addProperty(Constants.PARENT_ID, fields.get(Constants.PARENT).getAsJsonObject().get(Constants.KEY).getAsString());
			outputJson.addProperty(Constants.AREA_PATH, fields.get(Constants.PROJECT).getAsJsonObject().get(Constants.KEY).getAsString());
			if (KeysLoader.getRelease() != null) {
				outputJson.addProperty(Constants.RELEASE, KeysLoader.getRelease());
			}
			outputJson.addProperty(Constants.ITERATION_PATH, fields.get(Constants.SPRINT).getAsJsonObject().get(Constants.NAME).getAsString());
			outputJson.addProperty(Constants.ITERATION_STATE, fields.get(Constants.SPRINT).getAsJsonObject().get(Constants.STATE).getAsString());
			if (fields.get(Constants.TIME_TRACKING).getAsJsonObject().has(Constants.ORIGINAL_ESTIMATE)) {
				outputJson.addProperty(Constants.ORIGINAL_HOURS, getHours(fields.get(Constants.TIME_TRACKING).getAsJsonObject()
						.get(Constants.ORIGINAL_ESTIMATE).getAsString()));
			}
			if (fields.get(Constants.TIME_TRACKING).getAsJsonObject().has(Constants.REMAINING_ESTIMATE)) {
				outputJson.addProperty(Constants.REMAINING_HOURS, getHours(fields.get(Constants.TIME_TRACKING).getAsJsonObject()
						.get(Constants.REMAINING_ESTIMATE).getAsString()));
			}
			if (fields.get(Constants.TIME_TRACKING).getAsJsonObject().has(Constants.TIME_SPENT)) {
				outputJson.addProperty(Constants.COMPLETED_HOURS, getHours(fields.get(Constants.TIME_TRACKING)
						.getAsJsonObject().get(Constants.TIME_SPENT).getAsString()));
			}
			responseJson += outputJson.toString() + "\n";
		}
		return responseJson;
	}
	
	public int getHours(String estimate) {
		
		int hours = 0;
		StringTokenizer st = new StringTokenizer(estimate, " ");
		while (st.hasMoreTokens()) {
			String cal = st.nextToken();
			if (cal.contains("d")) {
				hours += Character.getNumericValue(cal.charAt(0)) * 
						Integer.parseInt(prop.getProperty(Constants.CAPACITY));
			} else {
				hours += Integer.parseInt(cal.split(Constants.REGEX)[0]);
			}
		}
		return hours;
	}
	
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("JIRA Report----->" + getJiraReport());
			provider.bulkSave(getJiraReport());
		}catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		}catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
