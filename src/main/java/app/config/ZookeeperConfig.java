package app.config;

import app.enums.LoginKeyEnum;
import app.util.Constants;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.EnumMap;

/**
 * @Author ws@yuan-mai.com
 * @Date 2020-09-21
 */
public class ZookeeperConfig {
    private static final Logger logger = LogManager.getLogger(ZookeeperConfig.class);
    private static final String PREFIX_KEY = "zookeeper";

    public static EnumMap<LoginKeyEnum, String> loadConfig() {
        File configFile = Constants.loadDefaultIniFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                Configuration configuration = configurations.ini(configFile);
                EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<>(LoginKeyEnum.class);
                loginEnumMap.put(LoginKeyEnum.HOST, configuration.getString(PREFIX_KEY + ".host", "zookeeper" ));
                loginEnumMap.put(LoginKeyEnum.PORT, configuration.getString(PREFIX_KEY + ".port", "2181" ));
                return loginEnumMap;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void saveConfig(EnumMap<LoginKeyEnum, String> loginEnumMap) {
        File configFile = Constants.loadDefaultIniFile();
        if (configFile != null) {
            ZookeeperConfig config = new ZookeeperConfig();
            Configurations configurations = new Configurations();
            try {
                FileBasedConfigurationBuilder<INIConfiguration> builder = configurations.iniBuilder(configFile);
                Configuration configuration = builder.getConfiguration();
                configuration.setProperty(PREFIX_KEY + ".host", loginEnumMap.get(LoginKeyEnum.HOST));
                configuration.setProperty(PREFIX_KEY + ".port", loginEnumMap.get(LoginKeyEnum.PORT));
                builder.save();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    private ZookeeperConfig() {}
}
