package loader;

public class LoaderFactory {

	public static final String JIRA_VELOCITY_METRICS = "JIRA_VELOCITY_METRICS";
	public static final String JIRA_BURNDOWN_METRICS = "JIRA_BURNDOWN_METRICS";
	public static final String SONAR = "SONAR";
	public static final String JENKINS = "JENKINS";
	public static final String WAM_JENKINS = "WAM_JENKINS";
	public static final String V1_VELOCITY = "V1_VELOCITY_METRICS";
	public static final String V1_BURNDOWN = "V1_BURNDOWN_METRICS";
	public static final String V1_DEFECTS = "V1_DEFECTS_METRICS";
	public static final String V1_UTIL = "V1_UTILIZATION";
	public static final String RTE_REPORT = "RTE_REPORT";
	public static final String RTE_DEFECTS_REPORT = "RTE_DEFECTS_REPORT";
	public static final String CPM = "CPM";
	public static final String WEBTEAM = "WEBTEAM";
	public static final String DB_HEALTH_REPORT = "DB_HEALTH_REPORT";
	public static final String SERVICE_HEALTH_REPORT = "SERVICE_HEALTH_REPORT";
	public static final String SONAR_TRACE = "SONAR_TRACE";

	public static Loader getLoader(String loaderType) {
		Loader loader = null;

		if (JIRA_VELOCITY_METRICS.equalsIgnoreCase(loaderType)) {
			loader = new JIRAVelocityLoader();
		} else if (JIRA_BURNDOWN_METRICS.equalsIgnoreCase(loaderType)) {
			loader = new JIRABurnDownLoader();
		} else if (SONAR.equalsIgnoreCase(loaderType)) {
			loader = new SONARLoader();
		}  else if (JENKINS.equalsIgnoreCase(loaderType)) {
			loader = new JenkinsLoader();
		}  else if (WAM_JENKINS.equalsIgnoreCase(loaderType)) {
			loader = new JenkinsLoader(true);
		} else if (V1_VELOCITY.equalsIgnoreCase(loaderType)) {
			loader = new V1VelocityLoader();
		} else if (V1_BURNDOWN.equalsIgnoreCase(loaderType)) {
			loader = new V1BurnDownLoader();
		} else if (V1_DEFECTS.equalsIgnoreCase(loaderType)) {
			loader = new V1DefectsLoader();
		} else if (V1_UTIL.equalsIgnoreCase(loaderType)) {
			loader = new UtilizationLoader();
		} else if (RTE_REPORT.equalsIgnoreCase(loaderType)) {
			loader = new RTELoader();
		} else if (RTE_DEFECTS_REPORT.equalsIgnoreCase(loaderType)) {
			loader = new RTEDefectsLoader();
		} else if (CPM.equalsIgnoreCase(loaderType)) {
			loader = new CPMLoader();
		} else if (WEBTEAM.equalsIgnoreCase(loaderType)) {
			loader = new WebteamLoader();
		} else if (DB_HEALTH_REPORT.equalsIgnoreCase(loaderType)) {
			loader = new DBHealthLoader();
		} else if (SERVICE_HEALTH_REPORT.equalsIgnoreCase(loaderType)) {
			loader = new ServiceHealthLoader();
		} else if (SONAR_TRACE.equalsIgnoreCase(loaderType)) {
			loader = new SonarTraceLoader();
		}

		return loader;
	}
}
