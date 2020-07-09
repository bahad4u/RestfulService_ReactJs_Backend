package loader;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

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

public class ServiceHealthLoader implements Loader{

	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(ServiceHealthLoader.class.getSimpleName());
	
	public String getServiceHealthReport() throws JSONException, Exception {

		log.info("Service Health Report method has started");
		String[] respData = new String[2];
		String xmlstr = null;
		String response = null;
		
//		String authType = prop.getProperty(Constants.DB_AUTH_TYPE);
//		String authValue = prop.getProperty(Constants.DB_AUTH_VALUE);
		String acceptheadertype = prop.getProperty(Constants.ACPT_HDR_TYPE);
		String acceptheadervalue = prop.getProperty(Constants.ACPT_HDR_VALUE);

		HashMap<String, String> headers = new HashMap<String, String>();
		//headers.put(authType, authValue);
		headers.put(acceptheadertype, acceptheadervalue);
		String uriInitial = prop.getProperty(Constants.SERVICE_HEALTH_URL);
		String[] uris = uriInitial.split("@");

		for (String uri : uris) {

			respData = HttpManager.httpCall(uri, headers, 
						Integer.parseInt(prop.getProperty(Constants.JOB_TIMEOUT)),
						null, "GET");
			xmlstr = respData[1];
		
			StringBuilder sb = new StringBuilder();
			JSONObject json = XML.toJSONObject(xmlstr);
			String data = json.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject jsondata = (JsonObject) parser.parse(data);
		
//			log.info("JSON data" + jsondata.toString());
			
			JsonArray serviceArray = jsondata.getAsJsonObject(Constants.HEALTH_CHK_STATUS).getAsJsonArray(Constants.SERVICE);
			String date = jsondata.getAsJsonObject(Constants.HEALTH_CHK_STATUS).get(Constants.TIME_STAMP).getAsString();
			
			for (JsonElement service : serviceArray) {

				String serviceName = service.getAsJsonObject().get(Constants.NAME).getAsString();

				JsonArray statusEntity = service.getAsJsonObject().get(Constants.STATUS).getAsJsonArray();

				sb.append(this.formatJsonEntity(statusEntity, date, serviceName));
			}
		
			response = sb.toString();
		}
		return response;
	}

	private String formatJsonEntity(JsonArray entity, String date, String serviceName) throws JSONException {
		
		String oneResource = "";
		
		for (JsonElement status : entity) {
			
			JsonObject statusValues = status.getAsJsonObject();
			
			String index = serviceName.concat(date.toString()).concat(statusValues.get(Constants.ENV_LC).getAsString().toString());
			
			JsonObject indexJo = new JsonObject();
			
			JsonObject objectJo = new JsonObject();
			objectJo.addProperty(Constants.INDEX, prop.getProperty(Constants.SERVICE_HEALTH_IDX));
			objectJo.addProperty(Constants.TYPE, prop.getProperty(Constants.JOB_TYPE));
			
			
			objectJo.addProperty(Constants.ID, index);
			indexJo.add(Constants.DATA_INDEX, objectJo);
			oneResource += indexJo.toString() + "\n";
			
			JsonObject hrObject = new JsonObject();
			
			
			
//			log.info("Checking for the logs->>>>>>>>>>>>>>>  " + statusValues);
			hrObject.addProperty(Constants.SERVICE, serviceName.toString());
			hrObject.addProperty(Constants.DATE, date);
			hrObject.addProperty(Constants.ENVIRONMENT, statusValues.get(Constants.ENV_LC).getAsString());
			
			if (statusValues.get(Constants.CONTENT).getAsString().toString().equals("NA")) {
				hrObject.addProperty(Constants.AVAIL_STATUS_LC, Constants.SERVICE_NA);
			} else {
				hrObject.addProperty(Constants.AVAIL_STATUS_LC, statusValues.get(Constants.CONTENT).getAsString());
			}

			oneResource += hrObject.toString() + "\n";
		}
		
		return oneResource;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Service Health Report----->" + getServiceHealthReport());
			provider.bulkSave(getServiceHealthReport());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
	
}
