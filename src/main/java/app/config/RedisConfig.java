package app.config;

import app.enums.LoginKeyEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.List;

/**
 * @Author pickjob@126.com
 * @Date 2020-09-21
 */
public class RedisConfig {
    private static final Logger logger = LogManager.getLogger(RedisConfig.class);
    private static final String PREFIX_KEY = "redis";
    private static final List<LoginKeyEnum> loginKeys = List.of(LoginKeyEnum.HOST, LoginKeyEnum.PORT, LoginKeyEnum.INDEX, LoginKeyEnum.PASSWORD);

    public static EnumMap<LoginKeyEnum, String> loadConfig() {
        return IniConfig.loadConfig(PREFIX_KEY, loginKeys);
    }

    public static void saveConfig(EnumMap<LoginKeyEnum, String> loginEnumMap) {
        IniConfig.saveConfig(PREFIX_KEY, loginEnumMap);
    }
}
