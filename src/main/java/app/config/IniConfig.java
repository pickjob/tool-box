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
import java.util.List;

/**
 * @Author pickjob@126.com
 * @Date 2020-09-28
 */
public class IniConfig {
    private static final Logger logger = LogManager.getLogger(IniConfig.class);

    public static EnumMap<LoginKeyEnum, String> loadConfig(String prefixKey, List<LoginKeyEnum> loginKeys) {
        File configFile = Constants.loadDefaultIniFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                Configuration configuration = configurations.ini(configFile);
                EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<>(LoginKeyEnum.class);
                for (LoginKeyEnum loginKey : loginKeys) {
                    loginEnumMap.put(loginKey, configuration.getString(prefixKey + "." + loginKey.name()));
                }
                return loginEnumMap;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void saveConfig(String prefixKey, EnumMap<LoginKeyEnum, String> loginEnumMap) {
        File configFile = Constants.loadDefaultIniFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                FileBasedConfigurationBuilder<INIConfiguration> builder = configurations.iniBuilder(configFile);
                Configuration configuration = builder.getConfiguration();
                for (LoginKeyEnum loginKey : loginEnumMap.keySet()) {
                    if (StringUtils.isNotBlank(loginEnumMap.get(loginKey))) {
                        configuration.setProperty(prefixKey + "." + loginKey.name(), loginEnumMap.get(loginKey));
                    }
                }
                builder.save();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
