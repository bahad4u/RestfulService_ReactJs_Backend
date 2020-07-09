package loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

public class CPMLoader implements Loader{
	
	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(CPMLoader.class.getSimpleName());
	
	public String processCPMData() throws LoaderException {
		
		log.info("CPM method has started");
		
		String line = null;
		String str = null;
		String response = "";
		String link = prop.getProperty(Constants.CPM_FILE_PATH);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(link));
			while ((line = br.readLine()) != null) 
			{   
				str += line;
			}
			JSONObject jsondata = XML.toJSONObject(str);
			response = this.formatJson(jsondata.getJSONObject(Constants.APP_CPM).getJSONArray(Constants.SERVICES));
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

			String cpmIndex = prop.getProperty(Constants.CPM_INDEX);
			String cpmType = prop.getProperty(Constants.CPM_TYPE);

			JsonObject objectJo = new JsonObject();
			objectJo.addProperty(Constants.INDEX, cpmIndex);
			objectJo.addProperty(Constants.TYPE, cpmType);
			objectJo.addProperty(Constants.ID,
					UUID.randomUUID().toString().replaceAll("-", ""));

			JsonObject indexJo = new JsonObject();
			indexJo.add(Constants.DATA_INDEX, objectJo);
			oneResource += indexJo.toString()+"\n";

			JsonObject cpmObject = new JsonObject();
			JSONObject cpmData = oneProject.getJSONObject(i);
			cpmObject.addProperty(Constants.REGION_LC, cpmData.getString(Constants.REGION_UC));
			cpmObject.addProperty(Constants.KPI_LC, cpmData.getString(Constants.KPI_UC));
			cpmObject.addProperty(Constants.BUDGET_LC, cpmData.getDouble(Constants.BUDGET_UC));
			cpmObject.addProperty(Constants.VALUE_LC, cpmData.getDouble(Constants.VALUE_UC));
			cpmObject.addProperty(Constants.APP_BUDGET_LC, cpmData.getDouble(Constants.APP_BUDGET_UC));
			cpmObject.addProperty(Constants.APP_VALUE_LC, cpmData.getDouble(Constants.APP_VALUE_UC));
			cpmObject.addProperty(Constants.INFRA_BUDGET_LC, cpmData.getDouble(Constants.INFRA_BUDGET_UC));
			cpmObject.addProperty(Constants.INFRA_VALUE_LC, cpmData.getDouble(Constants.INFRA_VALUE_UC));
			cpmObject.addProperty(Constants.BACKEND_BUDGET_LC, cpmData.getDouble(Constants.BACKEND_BUDGET_UC));
			cpmObject.addProperty(Constants.BACKEND_VALUE_LC, cpmData.getDouble(Constants.BACKEND_VALUE_UC));
			cpmObject.addProperty(Constants.ENV_LC, cpmData.getString(Constants.ENV_UC));
			cpmObject.addProperty(Constants.DATE, cpmData.getString(Constants.DATE_UC));
			cpmObject.addProperty(Constants.RELEASE, cpmData.getString(Constants.RELEASE_UC));
			oneResource += cpmObject.toString() + "\n";
		}
		return oneResource;
	}
	
	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
//			System.out.println("CPM Report----->" + processCPMData());
			provider.bulkSave(processCPMData());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(),esexp.getMessage(),esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(),httpex.getMessage(),httpex.getCause());
		} catch (Exception ex) {
			throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}
}
