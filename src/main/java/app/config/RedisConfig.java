package app.config;

import app.enums.LoginKey;
import app.util.IniConfigUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.List;

/**
 * @author pickjob@126.com
 * @date 2020-09-21
 */
public
class RedisConfig implements Config {
    private static final Logger logger = LogManager.getLogger(RedisConfig.class);
    private static final String PREFIX_KEY = "REDIS";
    private static final List<LoginKey> loginKeys = List.of(LoginKey.HOST, LoginKey.PORT, LoginKey.INDEX, LoginKey.PASSWORD);
    private String host;
    private String port;
    private String index;
    private String password;

    @Override
    public String getConfigProfile() {
        return PREFIX_KEY;
    }

    @Override
    public void initWithLoginEnumMap(EnumMap<LoginKey, String> loginEnumMap) {
        this.host = loginEnumMap.get(LoginKey.HOST);
        this.port = loginEnumMap.get(LoginKey.PORT);
        this.index = loginEnumMap.get(LoginKey.INDEX);
        this.password = loginEnumMap.get(LoginKey.PASSWORD);
    }

    @Override
    public EnumMap<LoginKey, String> loadLoginEnumMapFromFile() {
        return IniConfigUtil.loadConfig(PREFIX_KEY, loginKeys);
    }

    @Override
    public void writeLoginEnumMapToFile() {
        EnumMap<LoginKey, String> loginEnumMap = new EnumMap<>(LoginKey.class);
        loginEnumMap.put(LoginKey.HOST, getHost());
        loginEnumMap.put(LoginKey.PORT, getPort());
        loginEnumMap.put(LoginKey.INDEX, getIndex());
        loginEnumMap.put(LoginKey.PASSWORD, getPassword());
        IniConfigUtil.saveConfig(PREFIX_KEY, loginEnumMap);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
