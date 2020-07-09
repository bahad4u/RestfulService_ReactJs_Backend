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

public class DBHealthLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(DBHealthLoader.class.getSimpleName());
	
	public String getDBHealthReport() throws JSONException, Exception {
		
		log.info("DB Health Report method has started");
		String[] respData = new String[2];
		String xmlstr = null;
		String response = null;
		
		String authType = prop.getProperty(Constants.DB_AUTH_TYPE);
		String authValue = prop.getProperty(Constants.DB_AUTH_VALUE);
		String acceptheadertype = prop.getProperty(Constants.ACPT_HDR_TYPE);
		String acceptheadervalue = prop.getProperty(Constants.ACPT_HDR_VALUE);

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(authType, authValue);
		headers.put(acceptheadertype, acceptheadervalue);
		
		String uriInitial = prop.getProperty(Constants.ML_REPORT_URL);
		String[] uris = uriInitial.split("@");

		for (String uri : uris) {

			respData = HttpManager.httpCall(uri, headers, 
						Integer.parseInt(prop.getProperty(Constants.DB_REPORT_TIMEOUT)),
			            null, "GET");
			xmlstr = respData[1];
		
			StringBuilder sb = new StringBuilder();
			JSONObject json = XML.toJSONObject(xmlstr);
			String data = json.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject jsondata = (JsonObject) parser.parse(data);
		
//			log.info("JSON data" + jsondata.toString());
			
			JsonArray report = jsondata.getAsJsonObject(Constants.DB_HEALTH_RPRTS).getAsJsonArray(Constants.DB_HEALTH_RPRT);
			
			for (JsonElement healthreport : report)
			{
				String date = healthreport.getAsJsonObject().get(Constants.TIME_STAMP).getAsString();
				
				String runlevel = healthreport.getAsJsonObject().get(Constants.RUNLEVEL).getAsString();
				
				JsonObject cluster = healthreport.getAsJsonObject().get(Constants.CLUSTER).getAsJsonObject();
				
				if(cluster.get(Constants.ENTITY).isJsonArray())
				{
					sb.append(this.formatJsonEntity(
					        cluster.get(Constants.ENTITY).getAsJsonArray(), date,
					        cluster.get(Constants.SERVICE_HOST).getAsString(),
					        cluster.get(Constants.DOMAIN).getAsString(), runlevel));
				}
				else
				{
					sb.append(this.formatJsonObject(
					        cluster.get(Constants.ENTITY).getAsJsonObject(), date,
					        cluster.get(Constants.SERVICE_HOST).getAsString(),
					        cluster.get(Constants.DOMAIN).getAsString(), runlevel));
				}
			}
			response = sb.toString();
		}
		return response;
	}

	private String formatJsonEntity(JsonArray entity, String date, String servicehost, String domain, String runlevel) throws JSONException {

		String oneResource = "";
		
		for(JsonElement Jelem : entity) {

			JsonObject indexJo = new JsonObject();

			JsonObject hrObject = new JsonObject();
			JsonObject entitydata = Jelem.getAsJsonObject();
			
//			log.info("Checking for the logs->>>>>>>>>>>>>>>  " + entitydata);
			if (!entitydata.get(Constants.DATABASE).toString().equals("{}")) {

				String index = entitydata.get(Constants.DATABASE).getAsString().concat(date.toString());

				JsonObject objectJo = new JsonObject();
				objectJo.addProperty(Constants.INDEX, prop.getProperty(Constants.DB_HEALTH_IDX));
				// objectJo.addProperty(INDEX, "dbstatus");
				// objectJo.addProperty(INDEX, "healthreport");
				objectJo.addProperty(Constants.TYPE, prop.getProperty(Constants.DB_HEALTH_TYPE));
				// objectJo.addProperty(ID,UUID.randomUUID().toString().replaceAll("-",
				// ""));
				objectJo.addProperty(Constants.ID, index);
				indexJo.add(Constants.DATA_INDEX, objectJo);
				oneResource += indexJo.toString() + "\n";
				hrObject.addProperty(Constants.DATABASE, entitydata.get(Constants.DATABASE).getAsString());
				hrObject.add(Constants.REINDEXING, entitydata.get(Constants.RE_INDEXING));
				hrObject.add(Constants.MERGING, entitydata.get(Constants.MERGING));
				hrObject.addProperty(Constants.REINDEX, entitydata.get(Constants.RE_INDEXING).getAsString());
				hrObject.addProperty(Constants.MERGE, entitydata.get(Constants.MERGING).getAsString());
				if (entitydata.get(Constants.RE_IDXNG_COUNT).isJsonNull()
				        || entitydata.get(Constants.RE_IDXNG_COUNT).toString().equals("{}")) {
					hrObject.addProperty(Constants.REIDXNG_COUNT, 0);
				} else {
					hrObject.add(Constants.REIDXNG_COUNT, entitydata.get(Constants.RE_IDXNG_COUNT));
				}

				if (entitydata.get(Constants.MERGE_COUNT).isJsonNull() || entitydata
				        .get(Constants.MERGE_COUNT).toString().equals("{}")) {
					hrObject.addProperty(Constants.MERGECOUNT, 0);
				} else {
					hrObject.add(Constants.MERGECOUNT, entitydata.get(Constants.MERGE_COUNT));
				}

				if (entitydata.get(Constants.MERGE_SIZE).isJsonNull() || entitydata
				        .get(Constants.MERGE_SIZE).toString().equals("{}")) {
					hrObject.addProperty(Constants.MERGESIZE, 0);
				} else {
					hrObject.add(Constants.MERGESIZE, entitydata.get(Constants.MERGE_SIZE));
				}

				if (entitydata.get(Constants.DOC_COUNT).isJsonNull() || entitydata
				        .get(Constants.DOC_COUNT).toString().equals("{}")) {
					hrObject.addProperty(Constants.DOCCOUNT, 0);
				} else {
					hrObject.add(Constants.DOCCOUNT, entitydata.get(Constants.DOC_COUNT));
				}

				hrObject.add(Constants.AVAIL_STATUS, entitydata.get(Constants.AVAIL_STATUS_LC));
				hrObject.addProperty(Constants.DATE, date);
				hrObject.addProperty(Constants.ENV_LC, runlevel);
				hrObject.addProperty(Constants.SERVICE_HOST, servicehost);
				hrObject.addProperty(Constants.CLUSTER, domain);
				oneResource += hrObject.toString() + "\n";
			}
		}
		return oneResource;
	}
	
	private String formatJsonObject(JsonObject entity, String date, String servicehost, String domain, String runlevel) throws JSONException {

		String oneResource = "";

		JsonObject indexJo = new JsonObject();

		JsonObject hrObject = new JsonObject();
		JsonObject objectJo = new JsonObject();
		objectJo.addProperty(Constants.INDEX, Constants.DB_HEALTH_RPRT);
		objectJo.addProperty(Constants.TYPE, prop.getProperty(Constants.DB_HEALTH_TYPE));
	//	objectJo.addProperty(ID,UUID.randomUUID().toString().replaceAll("-", ""));
		objectJo.addProperty(Constants.ID,entity.get(Constants.DATABASE).toString().concat(date.toString()));
		indexJo.add(Constants.DATA_INDEX, objectJo);
		oneResource += indexJo.toString()+"\n";
		hrObject.addProperty(Constants.DATABASE, entity.get(Constants.DATABASE).getAsString());
		hrObject.add(Constants.REINDEXING, entity.get(Constants.RE_INDEXING));
		hrObject.add(Constants.MERGING, entity.get(Constants.MERGING));
		hrObject.addProperty(Constants.REINDEX, entity.get(Constants.RE_INDEXING).getAsString());
		hrObject.addProperty(Constants.MERGE, entity.get(Constants.MERGING).getAsString());
		if(entity.get(Constants.RE_IDXNG_COUNT).isJsonNull() || entity.get(Constants.RE_IDXNG_COUNT).toString().equals("{}"))
		{
			hrObject.addProperty(Constants.REIDXNG_COUNT, 0);
		}
		else
		{
			hrObject.add(Constants.REIDXNG_COUNT, entity.get(Constants.RE_IDXNG_COUNT));
		}
		
		if(entity.get(Constants.MERGE_COUNT).isJsonNull() || entity.get(Constants.MERGE_COUNT).toString().equals("{}"))
		{
			hrObject.addProperty(Constants.MERGECOUNT, 0);
		}
		else
		{
			hrObject.add(Constants.MERGECOUNT, entity.get(Constants.MERGE_COUNT));
		}
		
		if(entity.get(Constants.DOC_COUNT).isJsonNull() || entity.get(Constants.DOC_COUNT).toString().equals("{}"))
		{
			hrObject.addProperty(Constants.DOCCOUNT, 0);
		}
		else
		{
			hrObject.add(Constants.DOCCOUNT, entity.get(Constants.DOC_COUNT));
		}
		
		
		hrObject.add(Constants.AVAIL_STATUS, entity.get(Constants.AVAIL_STATUS));
		hrObject.addProperty(Constants.DATE, date);
		hrObject.addProperty(Constants.ENV_LC, runlevel);
		hrObject.addProperty(Constants.SERVICE_HOST, servicehost);
		hrObject.addProperty(Constants.CLUSTER, domain);
		oneResource += hrObject.toString() + "\n";
		return oneResource;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("DB Health Report----->" + getDBHealthReport());
			provider.bulkSave(getDBHealthReport());
		}catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		}catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
