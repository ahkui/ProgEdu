package fcu.selab.progedu.config;

import java.io.IOException;
import java.io.InputStream;

import java.security.Key;
import java.util.Properties;

import fcu.selab.progedu.exception.LoadConfigFailureException;
import io.jsonwebtoken.security.Keys;

public class ApplicationConfig {
  private static final String PROPERTY_FILE = "/config/course_config.properties";
  private static ApplicationConfig instance ;
  private static final String EXCEPTION = "Unable to get config of COURSE"
      + " connection string from file;";

  /**
   *
   * @return instance.
   * @throws URISyntaxException .
   */
  public static ApplicationConfig getInstance() {
    if (instance == null) {
      instance = new ApplicationConfig();
    }
    return instance;
  }

  private Properties props;

  private ApplicationConfig() {
    InputStream is = this.getClass().getResourceAsStream(PROPERTY_FILE);
    try {
      props = new Properties();
      props.load(is);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get tomcat server ip
   *
   * @return tomcat server ip
   * @throws LoadConfigFailureException when property file is not found, the
   *                                    exception is thrown
   */
  public String getTomcatServerIp() throws LoadConfigFailureException {
    String webHost = System.getenv("WEB_HOST");
    String webPort = System.getenv("WEB_PORT");
    System.out.println("WEB_HOST: " + webHost + " WEB_PORT: " + webPort);
    if (
        webHost != null && !webHost.equals("")
        && webPort != null && !webPort.equals("")
    ) {
      return webHost + ":" + webPort;
    }
    if (props != null) {
      return props.getProperty("COURSE_TOMCAT_SERVER_IP").trim();
    }
    throw new LoadConfigFailureException(EXCEPTION + PROPERTY_FILE);
  }

  /**
   * Get application base url
   *
   * @return application base url
   */
  public String getBaseurl() {
    String baseurl = System.getenv("WEB_API_BASEURL");
    System.out.println("WEB_API_BASEURL: " + baseurl);
    if (baseurl != null) {
      return baseurl;
    }
    return "";
  }
}
