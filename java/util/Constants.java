package util;

public interface Constants {

	//COMMON CONSTANTS
	public static final String Bulk = "/_bulk";
	public static final String search = "/_search";
	public static final String INDEX = "_index";
	public static final String TYPE ="_type";
	public static final String ID = "_id";
	public static final String JOB_TIMEOUT = "jobTimeout";
	public static final String JOB_TYPE = "jobType";
	public static final String DATA_INDEX = "index";
	public static final String SPRINT = "sprint";
	public static final String RELEASE = "release";
	public static final String RELEASE_UC = "Release";
	public static final String COMMITTED = "committed";
	public static final String DELIVERED = "delivered";
	public static final String VALUE = "value";
	public static final String PROJECT = "project";
	public static final String NAME = "name";
	public static final String DATE = "date";
	public static final String SERVICES = "Services";
	public static final String NIL = "NIL";
	public static final String JOBS = "jobs";
	public static final String LAST_BUILD = "lastBuild";
	public static final String PASS_COUNT = "passCount";
	public static final String FAIL_COUNT = "failCount";
	public static final String SKIP_COUNT = "skipCount";
	public static final String URL = "url";
	public static final String NUMBER = "number";
	public static final String QUOTES = "quotes";
	public static final String OFFSHORE = "Offshore";
	public static final String ONSITE = "Onsite";
	public static final String TRUE = "true";
	public static final String V1_CAPACITY = "Value";
	public static final String TIME_STAMP = "timestamp";
	public static final String ACPT_HDR_TYPE = "acceptHeaderType";
	public static final String ACPT_HDR_VALUE = "acceptHeaderValue";
	public static final String ACPT_HDR_JSON = "acceptHeaderValueJson";
	
	//ELASTIC CONSTANTS
	public static final String ELASTIC_TIMEOUT = "elasticTimeOut";
	public static final String ELASTIC_URL = "elasticUrl";
	public static final String GLOBAL_INDEX = "globalindex";
	public static final String GLOBAL_TYPE = "globaltype";
	public static final String SOURCE = "_source";
	
	//SONAR CONSTANTS
	public static final String FILTERED_PROJECTS = "filteredprojects";
	public static final String SONAR_TIMEOUT = "sonartimeout";
	public static final String SONAR = "sonar";
	public static final String SONAR_PARAM = "sonarparam";
	public static final String COMPONENTS = "components";
	public static final String COMPONENT = "component";
	public static final String PAGING = "paging";
	public static final String TOTAL = "total";
	public static final String PAGESIZE = "pageSize";
	public static final String SONAR_PROJECTS = "sonarprojects";
	public static final String SONAR_INDEX = "sonarindex";
	public static final String SONAR_TYPE = "sonartype";
	public static final String SERVICE = "service";
	public static final String MEASURES = "measures";
	public static final String METRIC = "metric";
	
	//JENKINS CONSTANT
	public static final String JENKINS_ID = "jenkins";
	public static final String JENKINS_PARAM = "jenkinsparam";
	public static final String WAM_JENKINS_ID = "wamjenkins";
	public static final String WAM_JENKINS_PARAM = "wamjenkinsparam";
	public static final String JENKINS_INDEX = "jenkinsindex";
	public static final String JENKINS_TYPE = "jenkinstype";
	public static final String JENKINS_TIMEOUT = "jenkinstimeout";
	public static final String JENKINS_TEST_REPORT = "jenkinstestreport";
	public static final String JENKINS_FILE = "JenkinsFile";
	public static final String BG_PIPELINE = "BlueGreenPipeline";
	public static final String ORCHESTRATOR = "Orchestrator";
	public static final String ORCHESTRATION = "Orchestration";
	public static final String ACTIONS = "actions";
	public static final String PARAMETERS = "parameters";
	public static final String ASSET_GROUP = "AssetGroup";
	public static final String CONTENT_ACQUIER = "contentacquier";
	public static final String NGINX_SOA = "nginxSoa";
	public static final String HEALTH_REPORT = "healthReport";
	public static final String RESULT = "result";
	
	//JIRA CONSTANTS
	public static final String JIRA_TIMEOUT = "jiraTimeout";
	public static final String JIRA_REPORT_INDEX = "jiraReportIndex";
	public static final String JIRA_MERTICS_INDEX = "jiraMetricsIndex";
	public static final String JIRA_TYPE = "jiraType";
	public static final String JIRA_AUTH_TYPE = "jiraAuthType";
	public static final String JIRA_AUTH_VALUE = "jiraAuthValue";
	public static final String JIRA_ACCEPT_TYPE = "jiraAcceptType";
	public static final String JIRA_FORMAT_TYPE = "jiraFormatType";
	public static final String JIRA_BURNDOWN_DATA = "burnDownData";
	public static final String JIRA_SPRINT_DATA = "sprintData";
	public static final String JIRA_QUERY_PARAM = "queryParam";
	public static final String RAPID_VIEW_URL = "rapidViewUrl";
	public static final String VIEWS = "views";
	public static final String SPRINT_URL = "sprintUrl";
	public static final String SPRINT_PARAM = "sprintParam";
	public static final String SPRINTS = "sprints";
	public static final String ID_VALUE = "id";
	public static final String ACTIVE_SPRINT = "ACTIVE";
	public static final String ISSUE = "issue";
	public static final String ISSUES = "issues";
	public static final String JIRA_BURNDOWN_QP = "burnDownQP";
	public static final String FIELDS = "fields";
	public static final String UPDATED = "updated";
	public static final String LAST_UPDATED = "lastUpdated";
	public static final String KEY = "key";
	public static final String WORKITEM_ID = "workItemId";
	public static final String WORKITEM_TYPE ="workItemType";
	public static final String PARENT = "parent";
	public static final String PARENT_ID = "parentId";
	public static final String AREA_PATH = "areaPath";
	public static final String ITERATION_PATH = "iterationPath";
	public static final String ITERATION_STATE = "iterationState";
	public static final String ISSUE_TYPE = "issuetype";
	public static final String STATE = "state";
	public static final String TIME_TRACKING = "timetracking";
	public static final String ORIGINAL_ESTIMATE = "originalEstimate";
	public static final String REMAINING_ESTIMATE = "remainingEstimate";
	public static final String TIME_SPENT = "timeSpent";
	public static final String ORIGINAL_HOURS = "originalHours";
	public static final String REMAINING_HOURS = "remainingHours";
	public static final String COMPLETED_HOURS = "completedHours";
	public static final String REGEX = "[a-z]+";
	public static final String CAPACITY = "capacityPerDay";
	public static final String CONTENTS = "contents";
	public static final String COMPLETED_ISSUES = "completedIssues";
	public static final String INCOMPLETE_ISSUES = "issuesNotCompletedInCurrentSprint";
	public static final String CURRENT_ESTIMATE = "currentEstimateStatistic";
	public static final String STATUS_NAME = "statusName";
	public static final String STORY_POINTS = "storyPoints";
	public static final String STAT_FIELD = "statFieldValue";
	public static final String TYPE_NAME = "typeName";
	
	//VERSION ONE CONSTANTS
	public static final String V1_TIMEOUT = "v1Timeout";
	public static final String V1_BURNDOWN_INDEX = "v1BurnDownIndex";
	public static final String V1_VELOCITY_INDEX = "v1VelocityIndex";
	public static final String V1_DEFECTS_INDEX = "v1DefectsIndex";
	public static final String V1_EPIC_INDEX = "v1EpicIndex";
	public static final String V1_UTIL_INDEX = "v1UtilizationIndex";
	public static final String V1_TYPE = "v1Type";
	public static final String V1_AUTH_TYPE = "v1AuthType";
	public static final String V1_AUTH_VALUE = "v1AuthValue";
	public static final String V1_BURNDOWN_URL = "v1BurnDownUrl";
	public static final String V1_VELOCITY_URL = "v1VelocityUrl";
	public static final String V1_DEFECTS_URL = "v1DefectsUrl";
	public static final String V1_EPIC_URL = "v1EpicUrl";
	public static final String V1_UTIL_URL = "v1UtilizationUrl";
	public static final String ACCEPT_HEADER = "acceptHeader";
	public static final String V1_BURNDOWN_QP = "v1BurnDownQP";
	public static final String V1_VELOCITY_QP = "v1VelocityQP";
	public static final String V1_DEFECTS_QP = "v1DefectsQP";
	public static final String V1_EPIC_QP = "v1EpicQP";
	public static final String V1_STORY_QP = "v1StoryQP";
	public static final String V1_UTIL_QP = "v1UtilizationQP";
	public static final String V1_TASK_QP = "v1TaskQP";
	public static final String BD_FILTER_PARAM = "bdfilterParam";
	public static final String VELOCITY_FILTER_PARAM = "velocityFilterParam";
	public static final String DEFECTS_FILTER_PARAM = "defectsFilterParam";
	public static final String EPIC_FILTER_PARAM = "epicFilterParam";
	public static final String FEATURE_FILTER_PARAM = "featureFilterParam";
	public static final String V1_UTIL_FILTER_PARAM = "v1UtlizationFP";
	public static final String V1_TASK_FILTER_PARAM = "v1TaskFP";
	public static final String V1_TASK_FILTER_PARAM1 = "v1TaskFP1";
	public static final String V1_UTIL_NAMELIST = "v1UtilizationNameList";
	public static final String ASSETS = "Assets";
	public static final String ATTRIBUTES = "Attributes";
	public static final String V1_NAME = "Name";
	public static final String ID_NUMBER = "ID.Number";
	public static final String PARENT_ESTIMATE = "Parent.Estimate";
	public static final String DETAIL_ESTIMATE = "DetailEstimate";
	public static final String TO_DO = "ToDo";
	public static final String ACTUALS = "Actuals.Value.@Sum";
	public static final String PARENT_STATUS = "Parent.Status.Name";
	public static final String READY = "Ready";
	public static final String TIMEBOX_STATE = "Timebox.AssetState";
	public static final String ACTIVE = "Active";
	public static final String FUTURE = "Future";
	public static final String CLOSED = "Closed";
	public static final String DONE = "Done";
	public static final String TIMEBOX_NAME = "Timebox.Name";
	public static final String ESTIMATE = "Estimate";
	public static final String V1_STATUS_NAME = "Status.Name";
	public static final String TEAM_NAME = "Team.Name";
	public static final String SCOPE_NAME = "Scope.Name";
	public static final String SEVERITY = "Custom_Severity.Name";
	public static final String OWNER = "Owners.Name";
	public static final String DESCRIPTION = "Description";
	public static final String EPIC_ID = "Epic.ID.Number";
	public static final String EPIC_NAME = "Epic.Name";
	public static final String FEATURE_ID = "Feature.ID.Number";
	public static final String FEATURE_NAME = "Feature.Name";
	public static final String CATEGORY_NAME = "Category.Name";
	public static final String STORY = "Story";
	public static final String NAME_UC = "Name";
	public static final String BUSINESS_VALUE = "Value";
	public static final String EPIC_BUSINESS_VALUE = "Epic.Value";
	public static final String EPIC_STATUS = "Epic.Status.Name";
	public static final String EPIC_TEAM = "Epic.Team.Name";
	public static final String EPIC_SCOPE = "Epic.Scope.Name";
	public static final String ASSET_TYPE = "AssetType";
	public static final String EPIC_CATEGORY = "Epic.Category.Name";
	public static final String EPIC_OWNERS = "Epic.Owners.Name";
	public static final String EPIC_ESTIMATE = "Epic.Estimate";
	public static final String SUBS_ESTIMATE = "SubsAndDown:PrimaryWorkitem.Estimate.@Sum";
	public static final String SUBS_DOWN_NUMBER = "SubsAndDown.Number";
	public static final String CHILD_TOTAL_HRS = "Children.DetailEstimate.@Sum";
	public static final String CHILD_CMPLTD_HRS = "Children.Actuals.Value.@Sum";
	public static final String CHILD_TODO_HRS = "Children.ToDo.@Sum";
	public static final String PARENT_EPIC_ID = "Parent.SuperAndUp.ID.Number";
	public static final String PARENT_EPIC_NAME = "Parent.SuperAndUp.Name";
	public static final String MEMBER = "Member.Name";
	
	//CPM CONTANTS
	public static final String CPM_FILE_PATH = "cpmFilePath";
	public static final String APP_CPM = "APP_CPM";
	public static final String CPM_INDEX = "cpmIndex";
	public static final String CPM_TYPE = "cpmType";
	public static final String REGION_LC = "region";
	public static final String REGION_UC = "Region";
	public static final String KPI_LC = "kpi";
	public static final String KPI_UC = "KPI";
	public static final String BUDGET_LC = "kpi_budget";
	public static final String BUDGET_UC = "KPI_Budget";
	public static final String VALUE_LC = "kpi_value";
	public static final String VALUE_UC = "KPI_Value";
	public static final String APP_BUDGET_LC = "kpi_app_budget";
	public static final String APP_BUDGET_UC = "KPI_APP_Budget";
	public static final String APP_VALUE_LC = "kpi_app_value";
	public static final String APP_VALUE_UC = "KPI_APP_Value";
	public static final String INFRA_BUDGET_LC = "kpi_infra_budget";
	public static final String INFRA_BUDGET_UC = "KPI_INFRA_Budget";
	public static final String INFRA_VALUE_LC = "kpi_infra_value";
	public static final String INFRA_VALUE_UC = "KPI_INFRA_Value";
	public static final String BACKEND_BUDGET_LC = "kpi_backend_budget";
	public static final String BACKEND_BUDGET_UC = "KPI_BackEnd_Budget";
	public static final String BACKEND_VALUE_LC = "kpi_backend_value";
	public static final String BACKEND_VALUE_UC = "KPI_BackEnd_Value";
	public static final String ENV_LC = "env";
	public static final String ENV_UC = "Env";
	public static final String DATE_UC = "Date";
	
	//WEBTEAM CONSTANTS
	public static final String WT_FILE_PATH = "wtFilePath"; 
	public static final String WEBTEAMS = "Webteams";
	public static final String WT_INDEX = "wtIndex";
	public static final String WT_TYPE = "wtType";
	public static final String DELIVERABLE_LC = "deliverable";
	public static final String DELIVERABLE_UC = "Deliverable";
	public static final String STATUS_LC = "status";
	public static final String STATUS_UC = "Status";
	public static final String SEVERITY_LC = "severity";
	public static final String SEVERITY_UC = "Severity";
	public static final String ROOT_CAUSE_LC = "rootCause";
	public static final String ROOT_CAUSE_UC = "Root_Cause";
	public static final String DSP_SEARCH = "DSP SEARCH";
	public static final String SEARCH = "Search";
	public static final String DSP_SEARCH_ALERTS = "DSP SEARCH ALERTS";
	public static final String SEARCH_ALERTS = "Search Alerts";
	public static final String DSP_RETR = "DSP RETRIEVER";
	public static final String RETRIEVER = "Retriever";
	public static final String WAM = "WAM";
	public static final String DQA_FF = "DIRECTED QUESTION & ANSWER";
	public static final String DQA = "DQA";
	public static final String DRS_SHEP = "DRSSHPD";
	public static final String SHEPARDS = "Shepards";
	public static final String DSP_LOADER = "DSP LOADER";
	public static final String LOADER = "Loader";
	public static final String ANALYTICS_UC = "ANALYTICS";
	public static final String ANALYTICS_LC = "Analytics";
	public static final String TOC = "TOC";
	public static final String POD_CATALOG_UC = "POD CATALOG";
	public static final String POD_CATALOG_LC = "Pod Catalog";
	public static final String RA_FF = "RECENT ACTIVITY SHARED SERVICE";
	public static final String RA = "Recent Activity";
	public static final String DOC_BUILDER_UC = "DOCUMENT BUILDER";
	public static final String DOC_BUILDER_LC = "Document Builder";
	public static final String BULK_DELIVERY_UC = "BULKDELIVERY";
	public static final String BULK_DELIVERY_LC = "Bulk Delivery";
	public static final String PUB_SUB_HUB = "PUBSUBHUB";
	public static final String PSH = "PSH";
	public static final String SOA2_NGINX = "SOA2 NGINX";
	public static final String NGINIX = "Nginix";
	public static final String SSA_FF = "SHARED STORAGE AREA";
	public static final String SSA = "SSA";
	public static final String ANSWERS_UC = "ANSWERS";
	public static final String ANSWERS_LC = "Answers";
	public static final String USA_UC = "USA USER DATA SUBSCRIBER";
	public static final String USA_LC = "USA User Data Subscriber";
	public static final String VCM = "VCM";
	public static final String WORK_FOLDERS_UC = "WORK FOLDERS";
	public static final String WORK_FOLDERS_LC = "Work Folders";
	public static final String VIEWSPEC_UC = "VIEWSPEC";
	public static final String VIEWSPEC_LC = "Viewspec";
	public static final String XSLT = "XSLT";
	public static final String LIST_FF = "LIST SHARED SERVICE";
	public static final String LIST = "List";
	public static final String DRS_CITE_CHECK = "NL DRSCITECK";
	public static final String CITE_CHECK = "Cite Check";
	public static final String DRS_JCITE = "NL DRSJCITE";
	public static final String JCITE = "JCite";
	public static final String CALC_ENGINE_UC = "CALCULATOR ENGINE";
	public static final String CALC_ENGINE_LC = "Calculator Engine";
	public static final String DSP_RMA = "DSP RMA";
	public static final String RMA = "RMA";
	public static final String PROFILE_SUITE_UC = "PROFILE SUITE";
	public static final String PROFILE_SUITE_LC = "Profile Suite";
	public static final String PERMA_LINK_UC = "PERMALINK";
	public static final String PERMA_LINK_LC = "Permalink";
	public static final String USER_PREFERENCE_UC = "USER PREFERENCE";
	public static final String USER_PREFERENCE_LC = "User Preference";
	
	//RTE CONSTANTS
	public static final String RTE_TIMEOUT = "itcTimeout";
	public static final String RTE_INDEX = "itcIndex";
	public static final String RTE_DEFECTS_INDEX = "rteDefectsIndex";
	public static final String RTE_TYPE = "itcType";
	public static final String RTE_PART_URL = "itcPartUrl";
	public static final String RTE_DEFECTS_URL = "rteDefectsUrl";
	public static final String RTE_QUERY_PARAM = "itcQueryParam";
	public static final String RTE_DEFECTS_QP = "rteDefectsQP";
	public static final String RTE_FILTER_PARAM = "rteFilterParam";
	public static final String SUITES = "suites";
	public static final String CASES = "cases";
	public static final String CLASS_NAME = "className";
	public static final String TEST_NAME = "testName";
	public static final String STATUS = "status";
	public static final String FIXED = "fixed";
	public static final String PASSED = "PASSED";
	public static final String FAILED = "FAILED";
	public static final String REGRESSION = "regression";
	public static final String RTE_BUILD_URL = "itcBuildUrl";
	public static final String RTE_BUILD_QP = "itcBuildQueryParam";
	public static final String LA_APP_REGRESSION = "LA-App_Regression";
	
	//DB HEALTH REPORT CONSTANTS
	public static final String DB_AUTH_TYPE = "dbAuthType";
	public static final String DB_AUTH_VALUE = "dbAuthValue";
	public static final String DB_REPORT_TIMEOUT = "dbReportTimeout";
	public static final String ML_REPORT_URL = "mlReportUrl";
	public static final String DB_HEALTH_RPRTS = "healthreports";
	public static final String DB_HEALTH_RPRT = "healthreport";
	public static final String RUNLEVEL = "runlevel";
	public static final String CLUSTER = "cluster";
	public static final String ENTITY = "entity";
	public static final String SERVICE_HOST = "servicehost";
	public static final String DOMAIN = "domain";
	public static final String DATABASE = "database";
	public static final String DB_HEALTH_IDX = "healthreportindex";
	public static final String DB_HEALTH_TYPE = "dbType";
	public static final String REINDEX = "reindex";
	public static final String RE_INDEXING = "re-indexing";
	public static final String REINDEXING = "reindexing";
	public static final String RE_IDXNG_COUNT = "re-indexing-count";
	public static final String REIDXNG_COUNT = "reindexingcount";
	public static final String MERGE = "merge";
	public static final String MERGING = "merging";
	public static final String MERGECOUNT = "mergecount";
	public static final String MERGE_COUNT = "merge-count";
	public static final String MERGESIZE = "mergesize";
	public static final String MERGE_SIZE = "merge-size";
	public static final String DOC_COUNT = "doc-count";
	public static final String DOCCOUNT = "doccount";
	public static final String AVAIL_STATUS = "availabilityStatus";
	public static final String AVAIL_STATUS_LC = "availabilitystatus";
	
	//SERVICE HEALTH CHECK CONSTANTS
	public static final String SERVICE_HEALTH_URL = "healthCheckUrl";
	public static final String HEALTH_CHK_STATUS = "healthcheckstatus";
	public static final String SERVICE_HEALTH_IDX = "serviceHealthIndex";
	public static final String ENVIRONMENT = "environment";
	public static final String CONTENT = "content";
	public static final String SERVICE_NA = "Service Not Available";
	
	//SONARE TRACE CONSTANTS
	public static final String SONAR_TRACE_URL = "sonarTraceUrl";
	public static final String SONAR_TRACE_USER = "sonarTraceUser";
	public static final String AFTER_PARAM = "createdAfter";
	public static final String BEFORE_PARAM = "createdBefore";
	public static final String SONAR_TRACE_PARAM = "sonarTraceParam";
	public static final String SONAR_TRACE_IDX = "sonarTraceIndex";
	public static final String AUTHOR = "author";
	public static final String CREATION_DATE = "creationDate";
	public static final String LINE = "line";
	public static final String LINE_NO = "line.no";
	public static final String NOT_SPECIFIED = "Not specified";
	public static final String MESSAGE = "message";
	
}