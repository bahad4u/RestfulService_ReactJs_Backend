package util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import loader.KeysLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JenkinsBean {
	
	private static Logger log = LogManager.getLogger(JenkinsBean.class.getSimpleName());
	private static String IN_PROGRESS = "In Progress";
	private static String DEV = "DEV";
	private static String CERT = "CERT";
	private static String PROD = "PROD"; 
	private static String NA = "NA"; 
	
	private String service;
	private String project = PropertyConfiguration.getProperties().getProperty(Constants.PROJECT);
	private int sprint = KeysLoader.getSprint();
	private String release = KeysLoader.getRelease();
	private long date = System.currentTimeMillis();
	private long lastUpdated;
	private String buildType;
	private String jobType;
	private String envType;
	private String environment;
	private long buildNo;
	private String status;
	private boolean hasTestCase;
	private long totalTestCase;
	private long failCount = 0;
	private long passCount = 0;
	private long skipCount = 0;
	private float percent;
	private String url;
	private JsonArray items = new JsonArray();
	private String triggeredjob;

	public String getService() {
		return service;
	}

	public void setService(String service) {
			this.service = service;
		}

	public String getTriggeredJob() {
		return triggeredjob;
	}

	public void setTriggeredJob(String triggeredjob) {
		this.triggeredjob = triggeredjob;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public int getSprint() {
		return sprint;
	}

	public void setSprint(int sprint) {
		this.sprint = sprint;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	public long getLastupdated() {
		return lastUpdated;
	}

	public void setLastupdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getEnvType() {
		return envType;
	}

	public void setEnvType(String envType) {
		if (envType != null) {
			if (envType.startsWith("d")) {
				this.envType = JenkinsBean.DEV;
			} else if (envType.startsWith("c")) {
				this.envType = JenkinsBean.CERT;
			} else if (envType.startsWith("p")) {
				this.envType = JenkinsBean.PROD;
			} else {
				this.envType = JenkinsBean.NA;
			}
		} else {
			this.envType = JenkinsBean.NA;
		}
	}

	public String getEnvValue() {
		return environment;
	}

	public void setEnvValue(String envTValue) {
		this.environment = envTValue;
	}

	public long getBuildNo() {
		return buildNo;
	}

	public void setBuildNo(long buildNo) {
		this.buildNo = buildNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		if(status != null)
		{
		this.status = status;
		}
		else
		{
		this.status = JenkinsBean.IN_PROGRESS;	
		}
			
	}

	public long getTotalTestCase() {
		return totalTestCase;
	}

	public void setTotalTestCase(long totalTestCase) {
		this.totalTestCase = totalTestCase;
	}

	public long getFailCount() {
		return failCount;
	}

	public void setFailCount(long failCount) {
		this.failCount = failCount;
	}

	public long getPassCount() {
		return passCount;
	}

	public void setPassCount(long passCount) {
		this.passCount = passCount;
	}

	public long getSkipCount() {
		return skipCount;
	}

	public void setSkipCount(long skipCount) {
		this.skipCount = skipCount;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean hasTestCase() {
		return hasTestCase;
	}

	public void setHasTestCase(boolean isTestCase) {
		this.hasTestCase = isTestCase;
	}

	public JsonArray getitems() {
		return items;
	}

	public void setVersion(JsonArray items) {
		this.items = items;
	}

	public JenkinsBean generateBean(JsonObject jo, HashMap<String, String> map) {

		// check for job containing cert or dev before calling this
		if (!jo.get(Constants.LAST_BUILD).isJsonNull()) {

			JsonObject lastBuildObj = new JsonObject();

			this.setUrl(jo.get(Constants.URL).getAsString());
			// check for health report
			if (jo.get(Constants.HEALTH_REPORT).getAsJsonArray().size() > 1) {
				this.setHasTestCase(true);
			} else {
				this.setHasTestCase(false);
			}
			try {
				lastBuildObj = jo.get(Constants.LAST_BUILD).getAsJsonObject();
				this.setLastupdated(lastBuildObj.get(Constants.TIME_STAMP).isJsonNull() ? 0:lastBuildObj.get(Constants.TIME_STAMP).getAsLong());
				this.setBuildNo(lastBuildObj.get(Constants.NUMBER).isJsonNull() ? 0:lastBuildObj.get(Constants.NUMBER).getAsLong());
				this.setStatus(lastBuildObj.get(Constants.RESULT).isJsonNull() ? "":lastBuildObj.get(Constants.RESULT).getAsString());
				this.setJobType(jo.get(Constants.NAME).isJsonNull() ? "":jo.get(Constants.NAME).getAsString());
				String value = "";

				if ((value = map.get(this.getJobType().toString())) != null) {
					log.debug("Map Value::: " + value);
					this.setEnvValue(value);
				} else {
					this.setEnvValue(JenkinsBean.NA);
				}
				if (this.getEnvValue() != null) {
					this.setEnvType(this.getEnvValue());
				}
			} catch (Exception e) {
				// System.out.println(lastBuildObj);
				e.printStackTrace();
			}
			return this;
		} else {
			this.setUrl("");
			this.setHasTestCase(false);
			this.setLastupdated(0);
			this.setBuildNo(0);
			this.setStatus(JenkinsBean.NA);
			this.setJobType("");
			this.setEnvValue("");
			this.setEnvType("");
			return this;
		}
	}

}
