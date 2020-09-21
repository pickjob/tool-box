package app.util;

import app.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: pickjob@126.com
 * @date: 2020-08-30
 **/
public class Constants {
    private static final Logger logger = LogManager.getLogger(Constants.class);
    private static final String DEFAULT_INI_FILE_NAME = "tools.ini";

    public static List<String> loadStyleSheets() {
        List<String> result = new ArrayList<>();
        result.add(App.class.getResource("/css/global.css").toExternalForm());
        return result;
    }

    public static File loadDefaultIniFile() {
        Object userHome = System.getProperties().get("user.home");
        if (userHome != null) {
            try {
                File configFile = new File(userHome + File.separator+ DEFAULT_INI_FILE_NAME);
                if (!configFile.exists()) {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();
                }
                return configFile;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
