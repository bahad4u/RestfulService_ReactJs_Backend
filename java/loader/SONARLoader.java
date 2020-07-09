package loader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.HttpManager;
import util.PropertyConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SONARLoader implements Loader {
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(SONARLoader.class.getSimpleName());
	private String PROJECT = prop.getProperty(Constants.PROJECT);

	ArrayList<String> list = new ArrayList<String>();
	ArrayList<String> filteredlist = new ArrayList<String>();

	public String sonarResourceData() throws LoaderException {
		log.info("Sonar Resource Data method has started");

		String jsonStr = null;
		String response = "";
		String[] respData = new String[2];

		try {
			this.projectList();
			log.info("List: " + list.size());
			String filteredprojects = prop.getProperty(Constants.FILTERED_PROJECTS);
			StringTokenizer st = new StringTokenizer(filteredprojects, ",");
			while (st.hasMoreTokens()) {
				String filteredproject = st.nextToken()
						.replaceAll("^\"|\"$", "").trim();
				filteredlist.add(filteredproject);
			}
			list.removeAll(filteredlist);
			log.info("Sonar Projects List :" + list.size());
			for (int i = 0; i < list.size(); i++) {
				String resource = list.get(i).toString()
						.replaceAll("^\"|\"$", "").trim();
				int timeout = Integer
						.parseInt(prop.getProperty(Constants.SONAR_TIMEOUT));
				respData = HttpManager.httpCall(prop.getProperty(Constants.SONAR) + resource + prop.getProperty(Constants.SONAR_PARAM), null,
								timeout, null, "GET");				
				if (Integer.parseInt(respData[0]) == 200) {
					jsonStr = respData[1];
//					log.info("Get Call Sonar :" + jsonStr);
					JsonParser jp = new JsonParser();
					JsonObject jo = jp.parse(jsonStr).getAsJsonObject()
							.getAsJsonObject(Constants.COMPONENT);
					response += this.formatJson(jo);
					// log.info("Each Sonar Response  :"+response);
				} else {
					continue;
				}
			}

		} catch (NullPointerException npex) {
			throw new LoaderException(npex.getMessage(), npex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(), ex.getCause());
		}
		//log.info("sonar response: "+response);
		return response;
	}

	public void projectList() throws IOException, LoaderException {
		
		log.info("Sonar Resource Data - ProjectList Method Started");
		
		int timeout = Integer.parseInt(prop.getProperty(Constants.SONAR_TIMEOUT));
		String[] responseData = new String[2];
		String jsonProjects = null;
		int pages=1;
		
		try {
			for(int i=0;i<pages;i++) {
				int curPage=i+1;
			//System.out.println("current page: "+curPage);
			responseData = HttpManager.httpCall(prop.getProperty(Constants.SONAR_PROJECTS)+"&p="+curPage, null, timeout, null, "GET");
			jsonProjects = responseData[1];			
			JsonParser jp = new JsonParser();
			JsonObject ps=(JsonObject) jp.parse(jsonProjects).getAsJsonObject().get(Constants.PAGING);			
			double total=ps.get(Constants.TOTAL).getAsDouble();
			double pageSize=ps.get(Constants.PAGESIZE).getAsDouble();
			pages=(int) Math.ceil(total/pageSize);	
			//System.out.println("total pages: "+pages);
			JsonArray ja = jp.parse(jsonProjects).getAsJsonObject().get(Constants.COMPONENTS).getAsJsonArray();
			for (JsonElement elem : ja) {
				String project1 = elem.getAsJsonObject().get(Constants.PROJECT).getAsString();
				list.add(project1);
			}
			}
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),
					httpex.getMessage(), httpex.getCause());
		}

	}

	public String formatJson(JsonObject oneProject) throws Exception {
		String oneResource = "";
		String sonarindex = prop.getProperty(Constants.SONAR_INDEX);
		String sonartype = prop.getProperty(Constants.SONAR_TYPE);
		LocalDateTime ldt = LocalDateTime.now();
		JsonObject objectJo = new JsonObject();
		objectJo.addProperty(Constants.INDEX, sonarindex);
		objectJo.addProperty(Constants.TYPE, sonartype);
		objectJo.addProperty(Constants.ID, UUID.randomUUID().toString().replaceAll("-", ""));
		JsonObject indexJo = new JsonObject();
		indexJo.add(Constants.DATA_INDEX, objectJo);
		oneResource += indexJo.toString() + "\n";
		JsonObject sonarObject = new JsonObject();
		sonarObject.addProperty(Constants.NAME, oneProject.get(Constants.NAME).getAsString());
		sonarObject.addProperty(Constants.DATE, ldt.toString());
		sonarObject.addProperty(Constants.SERVICE, oneProject.get(Constants.KEY).getAsString());
		sonarObject.addProperty(Constants.PROJECT, PROJECT);
		sonarObject.addProperty(Constants.SPRINT, KeysLoader.getSprint());
		sonarObject.addProperty(Constants.RELEASE, KeysLoader.getRelease());

		JsonArray jsonArray = oneProject.get(Constants.MEASURES).getAsJsonArray();
		for (JsonElement msrElement : jsonArray) {
			JsonObject jo = msrElement.getAsJsonObject();
			String key = jo.get(Constants.METRIC).getAsString();

			if (jo.has(Constants.VALUE)) {

				try {
					Number value1 = jo.get(Constants.VALUE).getAsNumber();
					sonarObject.addProperty(key, value1);
				} catch (NumberFormatException e) {
					String value1 = jo.get(Constants.VALUE).getAsString();
					sonarObject.addProperty(key, value1);
				}
			} else {
				continue;
			}
		}
		oneResource += sonarObject.toString() + "\n";
		return oneResource;
	}

	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
		System.out.println("Sonar Report----->" + sonarResourceData());
			provider.bulkSave(sonarResourceData());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(), esexp.getMessage(),
					esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),
					httpex.getMessage(), httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(), ex.getCause());
		}
	}
	
	
	/*
	 * public static void main(String[] args) throws IOException, LoaderException {
	 * SONARLoader sl=new SONARLoader(); sl.projectList(); log.info("List: " +
	 * list.size()); }
	 */
	 
}
