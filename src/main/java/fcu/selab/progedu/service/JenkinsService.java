package fcu.selab.progedu.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import fcu.selab.progedu.config.JenkinsConfig;
import fcu.selab.progedu.conn.StudentDashChoosePro;
import fcu.selab.progedu.exception.LoadConfigFailureException;
import fcu.selab.progedu.jenkins.JenkinsApi;
import fcu.selab.progedu.jenkins.JobStatus;
import fcu.selab.progedu.status.Status;
import fcu.selab.progedu.status.StatusFactory;

@Path("jenkins/")
public class JenkinsService {
  private static final String JOB = "/job/";
  JenkinsConfig jenkinsData = JenkinsConfig.getInstance();
  JenkinsApi jenkins = JenkinsApi.getInstance();
  JobStatus jobStatus = new JobStatus();

  /**
   * return string
   * 
   * @return "hello!"
   */
  @GET
  @Path("hello")
  @Produces(MediaType.TEXT_PLAIN)
  public Response sayHello() {
    String str = "hello! ";
    return Response.ok().entity(str).build();
  }

  /**
   * get project built color
   * 
   * @param proName
   *          project name
   * @param userName
   *          student name
   * @return color and commit count
   */

  public String getColor(String proName, String userName) {
    // ---Jenkins---
    String jobName = userName + "_" + proName;
    jobStatus.setName(jobName);
    String jobUrl = "";
    try {
      jobUrl = jenkinsData.getJenkinsHostUrl() + JOB + jobName;
    } catch (LoadConfigFailureException e) {
      e.printStackTrace();
    }
    jobStatus.setUrl(jobUrl + "/api/json?tree=color");

    // Get job status
    jobStatus.setJobApiJson();
    String apiJson = jobStatus.getJobApiJson();
    int commitCount = getProjectCommitCount(proName, userName);

    String color = null;
    if (null != apiJson) {
      color = jenkins.getJobJsonColor(apiJson);
    }

    String circleColor = "";
    if (commitCount == 1) {
      circleColor = "circle NB";
    } else {
      if (color != null) {
        circleColor = "circle " + color;
      } else {
        circleColor = "circle NB";
      }
    }
    return circleColor + "," + commitCount;
  }

  /**
   * get project commit count
   * 
   * @param proName
   *          project name
   * @param userName
   *          student name
   * @return count
   */
  @GET
  @Path("commits")
  @Produces(MediaType.APPLICATION_JSON)
  public int getProjectCommitCount(@QueryParam("proName") String proName,
      @QueryParam("userName") String userName) {
    // ---Jenkins---
    String jobName = userName + "_" + proName;
    jobStatus.setName(jobName);
    String jobUrl = "";
    List<Integer> numbers = new ArrayList<>();
    String jenkinsHostUrl = "";
    try {
      jenkinsHostUrl = jenkinsData.getJenkinsHostUrl();
      jobUrl = jenkinsHostUrl + JOB + jobName + "/api/json";
      numbers = jenkins.getJenkinsJobAllBuildNumber(jenkinsData.getJenkinsRootUsername(),
          jenkinsData.getJenkinsRootPassword(), jobUrl);
    } catch (LoadConfigFailureException e) {
      e.printStackTrace();
    }
    int commitCount = 0;
    for (int i : numbers) {
      jobStatus.setUrl(jenkinsHostUrl + JOB + jobName + "/" + i + "/api/json");
      // Get job status
      jobStatus.setJobApiJson();
      String apiJson = jobStatus.getJobApiJson();
      JSONObject json = new JSONObject(apiJson);
      JSONArray actions = json.getJSONArray("actions");
      JSONArray causes = actions.getJSONObject(0).getJSONArray("causes");
      String shortDescription = causes.getJSONObject(0).optString("shortDescription");
      if (shortDescription.contains("SCM")) {
        commitCount++;
      } else {
        if (i == 1) { // teacher commit
          commitCount++;
        }
      }
    }
    return commitCount;
  }

  /**
   * get student build detail info
   * 
   * @param num
   *          build num
   * @param userName
   *          student id
   * @param proName
   *          project name
   * @return build detail
   */
  @GET
  @Path("buildDetail")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBuildDetail(@QueryParam("num") int num,
      @QueryParam("userName") String userName, @QueryParam("proName") String proName) {
    StudentDashChoosePro stuDashChoPro = new StudentDashChoosePro();
    String buildApiJson = stuDashChoPro.getBuildApiJson(num, userName, proName);
    final String strDate = stuDashChoPro.getCommitTime(buildApiJson);
    String commitMessage = stuDashChoPro.getCommitMessage(num, userName, proName);
    String proType = proName.substring(0, 3);
    String status = stuDashChoPro.getCommitColor(num, userName, proName, buildApiJson, proType);
    String color = "circle " + status;
    JSONObject ob = new JSONObject();
    ob.put("num", num);
    ob.put("color", color);
    ob.put("date", strDate);
    ob.put("message", commitMessage);
    return Response.ok().entity(ob.toString()).build();
  }

  /**
   * get build error type
   * 
   * @param jenkinsData
   *          connect to jenkins
   * @param userName
   *          student id
   * @param proName
   *          project name
   * @param num
   *          build num
   * @return type
   */
  public static String checkErrorStyle(JenkinsConfig jenkinsData, String userName, String proName,
      int num) {
    StringBuilder jsonStringBuilder = new StringBuilder();
    try {
      HttpURLConnection connUrl = null;
      String consoleUrl = jenkinsData.getJenkinsHostUrl() + JOB + userName + "_" + proName + "/"
          + num + "/consoleText";
      URL url = new URL(consoleUrl);
      connUrl = (HttpURLConnection) url.openConnection();
      connUrl.setReadTimeout(10000);
      connUrl.setConnectTimeout(15000);
      connUrl.setRequestMethod("GET");
      connUrl.connect();
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(connUrl.getInputStream(), "UTF-8"));
      String line = "";
      while ((line = reader.readLine()) != null) {
        jsonStringBuilder.append(line);
      }
      reader.close();
    } catch (LoadConfigFailureException | IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
    return jsonStringBuilder.toString();
  }

  /**
   * get test folder
   * 
   * @param filePath
   *          folder directory
   * @return zip file
   */
  @GET
  @Path("getTestFile")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTestFile(@QueryParam("filePath") String filePath) {
    File file = new File(filePath);

    ResponseBuilder response = Response.ok((Object) file);
    response.header("Content-Disposition", "attachment;filename=");
    return response.build();
  }

  /**
   * 
   * @param url
   *          full console url
   * @return console
   */
  @POST

  @Path("getFeedbackInfo")
  public String getFeedbackInfo(String url) {
    JSONObject apiData = getColorTypeData(url);
    int num = apiData.getInt("num");
    String username = apiData.getString("username");
    String projName = apiData.getString("projName");

    String colorState = getColorType(num, username, projName);
    Status status = StatusFactory.getStatus(colorState);
    String detailConsoleText = jenkins.getConsoleText(url);
    String console = status.extractFailureMsg(detailConsoleText);

    return console;
  }

  /**
   * 
   * @param url
   *          jenkins consoleText url
   * @return colorTypeData: username �B project name �B commit number
   */
  private JSONObject getColorTypeData(String url) {
    JSONObject colorTypeData = new JSONObject();

    // username
    int startChar = url.indexOf("job") + 4;
    int endChar = url.indexOf("_");
    colorTypeData.put("username", url.substring(startChar, endChar));

    // projName
    startChar = endChar + 1;
    endChar = url.indexOf("/", startChar);
    colorTypeData.put("projName", url.substring(startChar, endChar));

    // num
    startChar = endChar + 1;
    endChar = url.indexOf("/", startChar);
    colorTypeData.put("num", Integer.valueOf(url.substring(startChar, endChar)));

    return colorTypeData;
  }

  /**
   * 
   * @param num
   *          commit number
   * @param userName
   *          user name
   * @param proName
   *          project name
   * @return color type
   */
  public String getColorType(int num, String userName, String proName) {
    StudentDashChoosePro stuDashChoPro = new StudentDashChoosePro();
    String buildApiJson = stuDashChoPro.getBuildApiJson(num, userName, proName);
    String commitMessage = stuDashChoPro.getCommitMessage(num, userName, proName);
    String proType = proName.substring(0, 3);
    commitMessage = commitMessage.replace("Commit message: ", "");
    if (null != commitMessage && !"".equals(commitMessage)) {
      commitMessage = commitMessage.substring(1, commitMessage.length() - 1);
    }

    String color = stuDashChoPro.getCommitColor(num, userName, proName, buildApiJson, proType);
    return color;
  }
}
