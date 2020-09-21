package app.config;

import app.enums.LoginKeyEnum;
import app.util.Constants;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.EnumMap;

/**
 * @Author ws@yuan-mai.com
 * @Date 2020-09-21
 */
public class RedisConfig {
    private static final Logger logger = LogManager.getLogger(RedisConfig.class);
    private static final String PREFIX_KEY = "redis";

    public static EnumMap<LoginKeyEnum, String> loadConfig() {
        File configFile = Constants.loadDefaultIniFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                Configuration configuration = configurations.ini(configFile);
                EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<>(LoginKeyEnum.class);
                loginEnumMap.put(LoginKeyEnum.HOST, configuration.getString(PREFIX_KEY + ".host", "redis" ));
                loginEnumMap.put(LoginKeyEnum.PORT, configuration.getString(PREFIX_KEY + ".port", "6379" ));
                loginEnumMap.put(LoginKeyEnum.INDEX, configuration.getString(PREFIX_KEY + ".index", "0" ));
                loginEnumMap.put(LoginKeyEnum.PASSWORD, configuration.getString(PREFIX_KEY + ".password", "" ));
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
            RedisConfig config = new RedisConfig();
            Configurations configurations = new Configurations();
            try {
                FileBasedConfigurationBuilder<INIConfiguration> builder = configurations.iniBuilder(configFile);
                Configuration configuration = builder.getConfiguration();
                configuration.setProperty(PREFIX_KEY + ".host", loginEnumMap.get(LoginKeyEnum.HOST));
                configuration.setProperty(PREFIX_KEY + ".port", loginEnumMap.get(LoginKeyEnum.PORT));
                configuration.setProperty(PREFIX_KEY + ".index", loginEnumMap.get(LoginKeyEnum.INDEX));
                if (StringUtils.isNotBlank(loginEnumMap.get(LoginKeyEnum.PASSWORD))) {
                    configuration.setProperty(PREFIX_KEY + ".password", loginEnumMap.get(LoginKeyEnum.PASSWORD));
                }
                builder.save();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    private RedisConfig() {}
}
