package loader;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
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

public class SonarTraceLoader implements Loader{

	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(SonarTraceLoader.class.getSimpleName());

	public String getSonarTraceData() throws HttpException, ElasticException, IOException, LoaderException {
		
		log.info("Sonar Traceability Data method has started");

		String jsonStr = null;
		String response = "";
		String[] respData = new String[2];
		int timeout = Integer.parseInt(prop.getProperty(Constants.JOB_TIMEOUT));
		String acceptheadertype = prop.getProperty(Constants.ACPT_HDR_TYPE);
		String acceptheadervalue = prop.getProperty(Constants.ACPT_HDR_JSON);

		HashMap<String, String> headers = new HashMap<String, String>();
		// headers.put(authType, authValue);
		headers.put(acceptheadertype, acceptheadervalue);

		// try {

		respData = HttpManager.httpCall(prop.getProperty(Constants.SONAR_TRACE_URL), headers, timeout, null, "GET");
		jsonStr = respData[1];
		//System.out.println("response: "+jsonStr);
		// LocalDate ld = LocalDate.now();
		// LocalDate ld1 = ld.plusDays(1);
		ZonedDateTime currentZonedatetime = ZonedDateTime.now();
		ZonedDateTime Last24Hours = currentZonedatetime.minusHours(24);
		/*System.out.println("currentZonedatetime ++++++++++++++++++ " + currentZonedatetime.toString() + "\n"
				+ "Last24Hours " + Last24Hours.toString());*/

		String date1 = currentZonedatetime.toString();
		String date2 = Last24Hours.toString();

		StringTokenizer st1 = new StringTokenizer(date1, "[");
		StringTokenizer st2 = new StringTokenizer(date2, "[");
		String currentDate = st1.nextToken();
		String minus24Hours = st2.nextToken();
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		StringTokenizer st3 = new StringTokenizer(currentDate, ".");
//		System.out.println("St3 count :" + st3.countTokens());
		sb1.append(st3.nextToken()).append("-0500");
		String formattedCurrentDate = sb1.toString();
		StringTokenizer st4 = new StringTokenizer(minus24Hours, ".");
		sb2.append(st4.nextToken()).append("-0500");
		String formattedminus24Hours = sb2.toString();
		/*System.out.println("Response " + "currentDate :" + currentDate + "minus24Hours :" + minus24Hours
				+ "formattedCurrentDate :" + formattedCurrentDate + "formattedminus24Hours :" + formattedminus24Hours);*/

		JsonParser jp = new JsonParser();
		JsonArray jo = jp.parse(jsonStr).getAsJsonObject().getAsJsonArray("authors");
		// System.out.println("**************"+jo.toString());
		
		HashSet<String> hs = new HashSet<String>();
		
		for (JsonElement traceElement : jo) {
			String name = traceElement.getAsString();
			if ((name.endsWith("net") || name.endsWith("com"))
					&& (name.contains("legal") || name.contains("lexisnexis"))) {
				hs.add(name);
			}
		}
		Iterator<String> Itr = hs.iterator();
		while(Itr.hasNext())
		{
			String url = prop.getProperty(Constants.SONAR_TRACE_USER) + Itr.next()
							+ prop.getProperty(Constants.AFTER_PARAM) + formattedminus24Hours 
							+ prop.getProperty(Constants.BEFORE_PARAM) + formattedCurrentDate
							+ prop.getProperty(Constants.SONAR_TRACE_PARAM);

			respData = HttpManager.httpCall(url, headers, timeout, null, "GET");
			jsonStr = respData[1];
			JsonArray jt = jp.parse(jsonStr).getAsJsonObject().getAsJsonArray("issues");
			if(jt.size() == 0)
			{
				continue;
			}
			else
			{
			response += formattraceJson(jt);
			}
		}
//		System.out.println(response);
		return response;
	}
	
	public String formattraceJson(JsonArray oneProject) throws LoaderException {
		
		String oneResource = "";
		String sonartraceindex = prop.getProperty(Constants.SONAR_TRACE_IDX);
		String sonartype = prop.getProperty(Constants.JOB_TYPE);
//		LocalDateTime ldt = LocalDateTime.now();

		for (JsonElement traceissue : oneProject) {

			JsonObject objectJo = new JsonObject();

			objectJo.addProperty(Constants.INDEX, sonartraceindex);
			objectJo.addProperty(Constants.TYPE, sonartype);
			objectJo.addProperty(Constants.ID, UUID.randomUUID().toString().replaceAll("-", ""));
			JsonObject indexJo = new JsonObject();
			indexJo.add(Constants.DATA_INDEX, objectJo);
			oneResource += indexJo.toString() + "\n";

			JsonObject sonarObject = new JsonObject();

			JsonObject issueobj = traceissue.getAsJsonObject();
			try {
				sonarObject.addProperty(Constants.AUTHOR, issueobj.get(Constants.AUTHOR).getAsString());
				sonarObject.addProperty(Constants.PROJECT, issueobj.get(Constants.PROJECT).getAsString());
				sonarObject.addProperty(Constants.CREATION_DATE, issueobj.get(Constants.CREATION_DATE).getAsString());
				sonarObject.addProperty(Constants.STATUS_LC, issueobj.get(Constants.STATUS_LC).getAsString());
				sonarObject.addProperty(Constants.COMPONENT, issueobj.get(Constants.COMPONENT).getAsString());
				sonarObject.addProperty(Constants.SEVERITY_LC, issueobj.get(Constants.SEVERITY_LC).getAsString());
				try {
					String Line = issueobj.get(Constants.LINE).getAsNumber().toString();
					sonarObject.addProperty(Constants.LINE_NO, Line);
				} catch (Exception e) {
					sonarObject.addProperty(Constants.LINE_NO, Constants.NOT_SPECIFIED);
				}

				sonarObject.addProperty(Constants.MESSAGE, issueobj.get(Constants.MESSAGE).getAsString());

				oneResource += sonarObject.toString() + "\n";
//				System.out.println(oneResource.toString());
			} catch (NullPointerException npe) {
				throw new LoaderException(npe.getMessage(), npe.getCause());
			} catch (Exception e) {
				throw new LoaderException(e.getMessage(), e.getCause());
			}
		}

		return oneResource;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Sonar Trace Report----->" + getSonarTraceData());
			provider.bulkSave(getSonarTraceData());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(), esexp.getMessage(), esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(), httpex.getMessage(), httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}

