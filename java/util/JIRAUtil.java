package util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import exceptions.ElasticException;
import exceptions.HttpException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JIRAUtil {
	
	Properties prop = PropertyConfiguration.getProperties();

	public HashMap<String, String> getboards(HashMap<String, String> headers, int timeout) throws HttpException, ElasticException, IOException {
		String[] boardList = new String[2];
		HashMap<String, String> boardsMap = new HashMap<String, String>();
		boardList = HttpManager.httpCall(prop.getProperty(Constants.RAPID_VIEW_URL), headers, timeout, null, "GET");
		JsonParser parser = new JsonParser();
		JsonObject boardsJO = parser.parse(boardList[1]).getAsJsonObject();
		JsonArray boardsArray = boardsJO.get(Constants.VIEWS).getAsJsonArray();
		for (JsonElement jElem : boardsArray) {
			boardsMap.put(jElem.getAsJsonObject().get(Constants.ID_VALUE).getAsString(),
					jElem.getAsJsonObject().get(Constants.NAME).getAsString());
		}
		return boardsMap;
	}

	public HashMap<String, String> getSprintId(HashMap<String, String> headers,
			int timeout, String rapidId) throws HttpException, ElasticException, IOException {
		String[] sprintsList = new String[2];
		HashMap<String, String> sprintMap = new HashMap<String, String>();
		sprintsList = HttpManager.httpCall(prop.getProperty(Constants.SPRINT_URL)
				+ rapidId + prop.getProperty(Constants.SPRINT_PARAM), headers, timeout, null, "GET");
		JsonParser parser = new JsonParser();
		JsonObject sprintJO = parser.parse(sprintsList[1]).getAsJsonObject();
		JsonArray sprintArray = sprintJO.get(Constants.SPRINTS).getAsJsonArray();
		for (JsonElement jElem : sprintArray) {
			if (jElem.getAsJsonObject().get(Constants.STATE).getAsString().equalsIgnoreCase(Constants.ACTIVE_SPRINT)) {
				sprintMap.put(jElem.getAsJsonObject().get(Constants.ID_VALUE).getAsString(),
						jElem.getAsJsonObject().get(Constants.NAME).getAsString());
			}
		}
		return sprintMap;
	}
}
