package loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.JsonObject;

public class WebteamLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(WebteamLoader.class.getSimpleName());
	
	public String processWTData() throws LoaderException {
		
		log.info("Webteam Loader methos has started");
		
		String line = null;
		String str = null;
		String response = "";
		String link = prop.getProperty(Constants.WT_FILE_PATH);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(link));
			while ((line = br.readLine()) != null) 
			{   
				str += line;
			}
			JSONObject jsondata = XML.toJSONObject(str);
			response = this.formatJson(jsondata.getJSONObject(Constants.WEBTEAMS).getJSONArray(Constants.SERVICES));
		} catch (FileNotFoundException fnfex) {
			throw new LoaderException(fnfex.getMessage(), fnfex.getCause());
		} catch (IOException ioex) {
			throw new LoaderException(ioex.getMessage(), ioex.getCause());
		} catch (JSONException jsonex) {
			throw new LoaderException(jsonex.getMessage(), jsonex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(), ex.getCause());
		}
		return response;
	}

	public String formatJson(JSONArray oneProject) throws JSONException {
		String oneResource = "";
		for(int i = 0; i < oneProject.length(); i++) {

			String wtIndex = prop.getProperty(Constants.WT_INDEX);
			String wtType = prop.getProperty(Constants.WT_TYPE);

			JsonObject objectJo = new JsonObject();
			objectJo.addProperty(Constants.INDEX, wtIndex);
			objectJo.addProperty(Constants.TYPE, wtType);
			objectJo.addProperty(Constants.ID,
					UUID.randomUUID().toString().replaceAll("-", ""));

			JsonObject indexJo = new JsonObject();
			indexJo.add(Constants.DATA_INDEX, objectJo);
			oneResource += indexJo.toString()+"\n";

			JsonObject wtObject = new JsonObject();
			JSONObject wtData = oneProject.getJSONObject(i);
			wtObject.addProperty(Constants.RELEASE, wtData.getString(Constants.RELEASE_UC));
			wtObject.addProperty(Constants.DELIVERABLE_LC, getDeliverable(wtData.getString(Constants.DELIVERABLE_UC)));
			wtObject.addProperty(Constants.STATUS_LC, wtData.getString(Constants.STATUS_UC));
			wtObject.addProperty(Constants.SEVERITY_LC, wtData.getDouble(Constants.SEVERITY_UC));
			wtObject.addProperty(Constants.ROOT_CAUSE_LC, wtData.getString(Constants.ROOT_CAUSE_UC));
			oneResource += wtObject.toString() + "\n";
		}
		return oneResource;
	}
	
	public String getDeliverable(String deliverable) {
		if(deliverable.contains(Constants.DSP_SEARCH)) {
			return Constants.SEARCH;
		} else if (deliverable.contains(Constants.DSP_SEARCH_ALERTS)) {
			return Constants.SEARCH_ALERTS;
		} else if (deliverable.contains(Constants.DSP_RETR)) {
			return Constants.RETRIEVER;
		} else if (deliverable.contains(Constants.WAM)) {
			return Constants.WAM;
		} else if (deliverable.contains(Constants.DQA_FF)) {
			return Constants.DQA;
		} else if (deliverable.contains(Constants.DRS_SHEP)) {
			return Constants.SHEPARDS;
		} else if (deliverable.contains(Constants.DSP_LOADER)) {
			return Constants.LOADER;
		} else if (deliverable.contains(Constants.ANALYTICS_UC)) {
			return Constants.ANALYTICS_LC;
		} else if (deliverable.contains(Constants.TOC)) {
			return Constants.TOC;
		} else if (deliverable.contains(Constants.POD_CATALOG_UC)) {
			return Constants.POD_CATALOG_LC;
		} else if (deliverable.contains(Constants.RA_FF)) {
			return Constants.RA;
		} else if (deliverable.contains(Constants.DOC_BUILDER_UC)) {
			return Constants.DOC_BUILDER_LC;
		} else if (deliverable.contains(Constants.BULK_DELIVERY_UC)) {
			return Constants.BULK_DELIVERY_LC;
		} else if (deliverable.contains(Constants.PUB_SUB_HUB)) {
			return Constants.PSH;
		} else if (deliverable.contains(Constants.SOA2_NGINX)) {
			return Constants.NGINIX;
		} else if (deliverable.contains(Constants.SSA_FF)) {
			return Constants.SSA;
		} else if (deliverable.contains(Constants.ANSWERS_UC)) {
			return Constants.ANSWERS_LC;
		} else if (deliverable.contains(Constants.USA_UC)) {
			return Constants.USA_LC;
		} else if (deliverable.contains(Constants.VCM)) {
			return Constants.VCM; 
		} else if (deliverable.contains(Constants.WORK_FOLDERS_UC)) {
			return Constants.WORK_FOLDERS_LC; 
		} else if (deliverable.contains(Constants.VIEWSPEC_UC)) {
			return Constants.VIEWSPEC_LC; 
		} else if (deliverable.contains(Constants.XSLT)) {
			return Constants.XSLT; 
		} else if (deliverable.contains(Constants.LIST_FF)) {
			return Constants.LIST; 
		} else if (deliverable.contains(Constants.DRS_CITE_CHECK)) {
			return Constants.CITE_CHECK; 
		} else if (deliverable.contains(Constants.DRS_JCITE)) {
			return Constants.JCITE; 
		} else if (deliverable.contains(Constants.CALC_ENGINE_UC)) {
			return Constants.CALC_ENGINE_LC; 
		} else if (deliverable.contains(Constants.DSP_RMA)) {
			return Constants.RMA; 
		} else if (deliverable.contains(Constants.PROFILE_SUITE_UC)) {
			return Constants.PROFILE_SUITE_LC; 
		} else if (deliverable.contains(Constants.PERMA_LINK_UC)) {
			return Constants.PERMA_LINK_LC; 
		} else if (deliverable.contains(Constants.USER_PREFERENCE_UC)) {
			return Constants.USER_PREFERENCE_LC; 
		}
		return deliverable;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("Webteam Report----->" + processWTData());
			provider.bulkSave(processWTData());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}
