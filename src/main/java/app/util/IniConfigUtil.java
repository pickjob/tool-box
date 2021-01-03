package app.util;

import app.enums.LoginKey;
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
 * @author pickjob@126.com
 * @date 2020-09-28
 */
public class IniConfigUtil {
    private static final Logger logger = LogManager.getLogger(IniConfigUtil.class);

    public static EnumMap<LoginKey, String> loadConfig(String prefixKey, List<LoginKey> loginKeys) {
        File configFile = ResourceUtils.loadDefaultIniConfigFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                Configuration configuration = configurations.ini(configFile);
                EnumMap<LoginKey, String> loginEnumMap = new EnumMap<>(LoginKey.class);
                for (LoginKey loginKey : loginKeys) {
                    loginEnumMap.put(loginKey, configuration.getString(prefixKey + "." + loginKey.name()));
                }
                return loginEnumMap;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void saveConfig(String prefixKey, EnumMap<LoginKey, String> loginEnumMap) {
        File configFile = ResourceUtils.loadDefaultIniConfigFile();
        if (configFile != null) {
            Configurations configurations = new Configurations();
            try {
                FileBasedConfigurationBuilder<INIConfiguration> builder = configurations.iniBuilder(configFile);
                Configuration configuration = builder.getConfiguration();
                for (LoginKey loginKey : loginEnumMap.keySet()) {
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
