package app.config;

import app.enums.LoginKey;

import java.util.EnumMap;

/**
 * @author pickjob@126.com
 * @date 2020-12-31
 */
public interface Config {
    String getConfigProfile();

    void initWithLoginEnumMap(EnumMap<LoginKey, String> loginEnumMap);

    EnumMap<LoginKey, String> loadLoginEnumMapFromFile();

    void writeLoginEnumMapToFile();
}
