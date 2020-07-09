package loader;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import elasticsearch.ElasticServiceProvider;
import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import util.Constants;
import util.HttpManager;
import util.JenkinsBean;
import util.LogManager;
import util.PropertyConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JenkinsLoader implements Loader {

	Properties prop = PropertyConfiguration.getProperties();
	private static Logger log = LogManager.getLogger(JenkinsLoader.class.getSimpleName());

	HashMap<String, String> map = new HashMap<>();
	String jenkinsindex = prop.getProperty(Constants.JENKINS_INDEX);
	String jenkinstype = prop.getProperty(Constants.JENKINS_TYPE);
	boolean isWamJob = false;
	boolean bluegreen = false;

	int count = 0;

	public JenkinsLoader() {

	}

	public JenkinsLoader(boolean wamJob) {
		this.isWamJob = wamJob;
	}

	public String getJenkinsData() throws NumberFormatException, HttpException, IOException, LoaderException {
		
		log.info("Jenkins Data method has started");
		System.out.println("Jenkins Data method has started");
		String[] respData = new String[2];
		respData =
		    (isWamJob)
		            ? HttpManager.httpCall(
		                    prop.getProperty(Constants.WAM_JENKINS_ID) + prop
                            .getProperty(Constants.WAM_JENKINS_PARAM),
                    null,
                    Integer.parseInt(prop.getProperty(Constants.JENKINS_TIMEOUT)),
                    null, "GET")
		            : HttpManager.httpCall(
		                    prop.getProperty(Constants.JENKINS_ID)
                            + prop.getProperty(Constants.JENKINS_PARAM),
                    null,
                    Integer.parseInt(prop.getProperty(Constants.JENKINS_TIMEOUT)),
                    null, "GET");	

		return getJenkinsReport(respData[1]);
	}

	public String getJenkinsReport(String jsonStr) throws LoaderException {

		JsonParser jp = new JsonParser();
		JsonObject jpo = jp.parse(jsonStr).getAsJsonObject();
		StringBuilder responseJsonStr = new StringBuilder();
		if (jpo.has(Constants.JOBS)) {
			JsonArray ja = jp.parse(jsonStr).getAsJsonObject().get(Constants.JOBS).getAsJsonArray();
			this.serviceJobs(ja, responseJsonStr);
		}	
		System.out.println("Count: " + count);

		System.out.println("Jenkins Response: " + responseJsonStr.toString());

		return responseJsonStr.toString();
	}

	public void serviceJobs(JsonArray ja, StringBuilder responseJsonStr) throws LoaderException {
		for (JsonElement jsonElement : ja) {
			JenkinsBean bean = new JenkinsBean();
			bean.setService(jsonElement.getAsJsonObject().get(Constants.NAME).getAsString());
//			log.info("Checking for the logs " + jsonElement.getAsJsonObject().get(Constants.NAME).getAsString());
			JsonElement jobs = jsonElement.getAsJsonObject().get(Constants.JOBS);

			if (jobs == null)
				continue;
			if (jobs.isJsonArray()) {
				JsonArray ja1 = jobs.getAsJsonArray();
				this.checkBuildJobs(ja1, bean);
				if (bluegreen == true) {
					map.clear();
					this.buildJobs(ja1, responseJsonStr, bean);
					bluegreen = false;
				} else {
					this.serviceJobs(ja1, responseJsonStr);
				}
			}
		}
	}

	public void checkBuildJobs(JsonArray ja1, JenkinsBean bean) {
		for (JsonElement jsonElement1 : ja1) {
			String servicejob = jsonElement1.getAsJsonObject().get(Constants.NAME).getAsString();
			if ((Constants.JENKINS_FILE.equalsIgnoreCase(servicejob))
					|| (Constants.BG_PIPELINE.equalsIgnoreCase(servicejob))
					|| servicejob.endsWith(Constants.BG_PIPELINE) || servicejob.endsWith(Constants.ORCHESTRATOR)) {
				bluegreen = true;
			}
		}
}

	public void buildJobs(JsonArray ja1, StringBuilder responseJsonStr, JenkinsBean bean) throws LoaderException {
		this.bluegreenjob(ja1, bean.getService());
		this.actualJobs(ja1, responseJsonStr, bean);
	}

	public void bluegreenjob(JsonArray ja1, String s) {
		for (JsonElement jsonElement1 : ja1) {
			String servicejob = jsonElement1.getAsJsonObject().get(Constants.NAME).getAsString();
			if ((Constants.BG_PIPELINE.equalsIgnoreCase(servicejob) || servicejob.endsWith(Constants.BG_PIPELINE)) && jsonElement1.getAsJsonObject().get(Constants.JOBS)!=null) {

			//if (Constants.BG_PIPELINE.equalsIgnoreCase(servicejob) || servicejob.endsWith(Constants.BG_PIPELINE)) {
				JsonArray ja22 = jsonElement1.getAsJsonObject().get(Constants.JOBS).getAsJsonArray();
				for (JsonElement jsonElementnew : ja22) {
					JsonObject oneJobnew = jsonElementnew.getAsJsonObject();
					if (!oneJobnew.get(Constants.LAST_BUILD).isJsonNull()) {
						JsonArray action = oneJobnew.get(Constants.LAST_BUILD).getAsJsonObject().get(Constants.ACTIONS)
								.getAsJsonArray();
						JsonArray parameter = new JsonArray();
						for (JsonElement jsonElementnew1 : action) {
							if (jsonElementnew1.getAsJsonObject().has(Constants.PARAMETERS)) {
								parameter = jsonElementnew1.getAsJsonObject().get(Constants.PARAMETERS)
										.getAsJsonArray();
								for (JsonElement jsonElementnew2 : parameter) {
									String value = jsonElementnew2.getAsJsonObject().get(Constants.NAME).getAsString();

									if (value != null && Constants.ASSET_GROUP.equalsIgnoreCase(value)) {
										map.put(oneJobnew.get(Constants.NAME).getAsString(),
												jsonElementnew2.getAsJsonObject().get(Constants.VALUE).getAsString());
									} else {
										continue;
									}
								}
							}
						}

					}
				}
			}
		}
//		log.info("Map1" + map);

	}

	public void actualJobs(JsonArray ja1, StringBuilder responseJsonStr, JenkinsBean bean) throws LoaderException {
		for (JsonElement jsonElement1 : ja1) {
			String servicejob = jsonElement1.getAsJsonObject().get(Constants.NAME).getAsString();
			if ((servicejob.endsWith(Constants.ORCHESTRATOR) || (Constants.ORCHESTRATION.equalsIgnoreCase(servicejob))
					|| (Constants.CONTENT_ACQUIER.equalsIgnoreCase(servicejob))
					|| (Constants.NGINX_SOA.equalsIgnoreCase(servicejob))
					|| (Constants.JENKINS_FILE.equalsIgnoreCase(servicejob)
							|| bean.getService().equalsIgnoreCase(servicejob)
							|| servicejob.endsWith(bean.getService().substring(bean.getService().length() / 2))))
					&& !map.isEmpty()) {
				bean.setTriggeredJob(servicejob);
//				log.info("checking for servicejob/TriggeredJob: " + jsonElement1);
				if (jsonElement1.getAsJsonObject().has(Constants.JOBS)
						&& jsonElement1.getAsJsonObject().get(Constants.JOBS).isJsonArray()) {
					JsonArray ja2 = jsonElement1.getAsJsonObject().get(Constants.JOBS).getAsJsonArray();

					for (JsonElement jsonElement2 : ja2) {
						
						boolean flag = false;
						JsonObject oneJob = jsonElement2.getAsJsonObject();
						String jobType = oneJob.get(Constants.NAME).getAsString();
//						System.out.println("Job Type---->" + oneJob);
						bean = bean.generateBean(oneJob, map);
						
						long currentDateTime = bean.getDate();
						long lastUpdate = bean.getLastupdated();
						flag = checkDate(currentDateTime, lastUpdate);
						if (flag == true) {
						
							if (!bean.equals(null)) {
								if (bean.hasTestCase()) {
									bean = this.getTestReportForJob(bean);
	
									JsonObject objectJo = new JsonObject();
									objectJo.addProperty(Constants.INDEX, Constants.JENKINS_INDEX);
									objectJo.addProperty(Constants.TYPE, Constants.JENKINS_TYPE);
									objectJo.addProperty(Constants.ID, UUID.randomUUID().toString().replaceAll("-", ""));
									JsonObject indexJo = new JsonObject();
									indexJo.add(Constants.DATA_INDEX, objectJo);
	
									responseJsonStr.append(indexJo.toString() + "\n");
//									log.info("checking bean object" + new Gson().toJson(bean));
									responseJsonStr.append(new Gson().toJson(bean) + "\n");
									count++;
								} else if (!bean.getJobType().isEmpty()) {
									bean.setFailCount(0);
									bean.setPassCount(0);
									bean.setSkipCount(0);
									bean.setTotalTestCase(bean.getPassCount() + bean.getFailCount() + bean.getSkipCount());
									bean.setPercent(0);
	
									JsonObject objectJo = new JsonObject();
									objectJo.addProperty(Constants.INDEX, Constants.JENKINS_INDEX);
									objectJo.addProperty(Constants.TYPE, Constants.JENKINS_TYPE);
									objectJo.addProperty(Constants.ID, UUID.randomUUID().toString().replaceAll("-", ""));
									JsonObject indexJo = new JsonObject();
									indexJo.add(Constants.DATA_INDEX, objectJo);
	
									responseJsonStr.append(indexJo.toString() + "\n");
									responseJsonStr.append(new Gson().toJson(bean) + "\n");
									count++;
								}
							}
						} else {
							continue;
						}
					}
				} else {
					continue;
				}
			} else {
				continue;
			}
		}
	}
	
	private static boolean checkDate(long currentDateTime, long lastUpdate) {

		boolean check = false;
		Date currentDate = new Date(currentDateTime);
//		log.info("currentDate :" + currentDate);
		// 1535138227022
		// 1529727983201
		// 1536791548319

		Date lastUpdated = new Date(lastUpdate);
//		log.info("lastUpdated :" + lastUpdated);

		DateTime d1 = new DateTime(currentDate);
		DateTime d2 = new DateTime(lastUpdated);

		int days = Days.daysBetween(d2, d1).getDays();

		int hours = Hours.hoursBetween(d2, d1).getHours();

		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

		/*log.info("GetDate: " + d1 + " LastUpdated: " + d2 + " days : " + days
		        + "days" + " hours: " + hours + "hours" + " dayOfWeek: "
		        + dayOfWeek);*/
		if (dayOfWeek == 2 && days <= 2) {
//			log.info("Monday");
			check = true;
		} else if (days == 0 && hours < 24) {
//			log.info("Week days");
			check = true;
		}
		return check;
	}

	public JenkinsBean getTestReportForJob(JenkinsBean bean) throws LoaderException {

		JsonObject jo = new JsonObject();
		String jsonStr = null;
		String[] respData = new String[2];
		try {
			respData = HttpManager.httpCall(
					bean.getUrl() + bean.getBuildNo() + prop.getProperty(Constants.JENKINS_TEST_REPORT), null,
					Integer.parseInt(prop.getProperty(Constants.JENKINS_TIMEOUT)), null, "GET");
			if (Integer.parseInt(respData[0]) == 200) {
				jsonStr = respData[1];

//				log.info("Bean Response: " + jsonStr);

				JsonParser jp = new JsonParser();
				jo = jp.parse(jsonStr).getAsJsonObject();

				bean.setFailCount(jo.get(Constants.FAIL_COUNT).getAsLong());
				bean.setPassCount(jo.get(Constants.PASS_COUNT).getAsLong());
				bean.setSkipCount(jo.get(Constants.SKIP_COUNT).getAsLong());
				bean.setTotalTestCase(bean.getPassCount() + bean.getFailCount() + bean.getSkipCount());

				float val = (bean.getPassCount() / bean.getTotalTestCase()) * 100;

				// System.out.print(val);

				bean.setPercent(val);
			}

		}

		catch (Exception e) {
			throw new LoaderException(e.getMessage(),e.getCause());
		}

		return bean;
	}

	public void load() throws LoaderException {
		try {
			ElasticServiceProvider provider = new ElasticServiceProvider();
		System.out.println("Jenkins Report----->" + getJenkinsData());
			provider.bulkSave(getJenkinsData());
		} catch (ElasticException esexp) {
			throw new LoaderException(esexp.getErrorCode(), esexp.getMessage(), esexp.getCause());
		} catch (HttpException httpex) {
			throw new LoaderException(httpex.getErrorCode(), httpex.getMessage(), httpex.getCause());
		} catch (Exception ex) {
			 throw new LoaderException(ex.getMessage(),ex.getCause());
		}
	}

}
