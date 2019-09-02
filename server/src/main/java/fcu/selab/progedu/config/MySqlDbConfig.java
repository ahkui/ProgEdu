package fcu.selab.progedu.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fcu.selab.progedu.exception.LoadConfigFailureException;

public class MySqlDbConfig {

  private static final String PROPERTY_FILE = "/config/db_config.properties";

  private static MySqlDbConfig instance;

  /**
   *
   * @return MySqlDbConfig
   */
  public static MySqlDbConfig getInstance() {
    if (instance == null) {
      instance = new MySqlDbConfig();
    }
    return instance;
  }

  private Properties props;

  private MySqlDbConfig() {

    InputStream is = this.getClass().getResourceAsStream(PROPERTY_FILE);
    try {
      props = new Properties();
      props.load(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get database connection string
   *
   * @return db_connection
   * @throws LoadConfigFailureException on properties call error
   */
  public String getDbConnectionString() throws LoadConfigFailureException {
    String dbType = System.getenv("DB_TYPE");
    System.out.println("DB_TYPE: " + dbType);
    if (dbType != null && !dbType.equals("")) {
      return "jdbc:" + dbType + "://" + getDbHost() + "/" + getDbSchema()
          + "?relaxAutoCommit=true&useSSL=false&useUnicode=true&characterEncoding=utf-8";
    }
    if (props != null) {
      return props.getProperty("DB_CONNECTION").trim();
    }
    throw new LoadConfigFailureException(
        "Unable to get config of MYSQL connection string from file;" + PROPERTY_FILE);
  }

  /**
   * Get database user
   *
   * @return user
   * @throws LoadConfigFailureException on properties call error
   */
  public String getDbUser() throws LoadConfigFailureException {
    String dbUser = System.getenv("DB_USER");
    System.out.println("DB_USER: " + dbUser);
    if (dbUser != null && !dbUser.equals("")) {
      return dbUser;
    }
    if (props != null) {
      return props.getProperty("DB_USER").trim();
    }
    throw new LoadConfigFailureException(
        "Unable to get config of MYSQL user from file;" + PROPERTY_FILE);
  }

  /**
   * Get database password
   *
   * @return password
   * @throws LoadConfigFailureException on properties call error
   */
  public String getDbPassword() throws LoadConfigFailureException {
    String dbPassword = System.getenv("DB_PASSWORD");
    System.out.println("DB_PASSWORD: " + dbPassword);
    if (dbPassword != null && !dbPassword.equals("")) {
      return dbPassword;
    }
    if (props != null) {
      return props.getProperty("DB_PASSWORD").trim();
    }
    throw new LoadConfigFailureException(
        "Unable to get config of MYSQL password from file;" + PROPERTY_FILE);
  }

  /**
   * Get database schema
   *
   * @return database schema
   */
  public String getDbSchema() {
    String dbSchema = System.getenv("DB_DATABASE");
    System.out.println("DB_DATABASE: " + dbSchema);
    if (dbSchema != null && !dbSchema.equals("")) {
      return dbSchema;
    }
    return "ProgEdu";
  }

  /**
   * Get database host
   *
   * @return database host
   */
  public String getDbHost() {
    String dbHost = System.getenv("DB_HOST");
    System.out.println("DB_HOST: " + dbHost);
    if (dbHost != null && !dbHost.equals("")) {
      return dbHost;
    }
    return "db";
  }
}
