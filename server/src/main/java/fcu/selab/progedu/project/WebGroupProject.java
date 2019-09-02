package fcu.selab.progedu.project;

import fcu.selab.progedu.status.StatusEnum;

public class WebGroupProject extends GroupProjectType {

  @Override
  public ProjectTypeEnum getProjectType() {
    return ProjectTypeEnum.WEB;
  }

  @Override
  public String getSampleTemplate() {
    return "WebQuickStart.zip";
  }

  @Override
  public String getJenkinsJobConfigSample() {
    return "group_web_config.xml";
  }

  @Override
  public void createJenkinsJobConfig(String username, String projectName) {

  }

  @Override
  public StatusEnum checkStatusType(int num, String username, String assignmentName) {
    return null;
  }

}
