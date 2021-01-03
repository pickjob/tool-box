package app.controller.common;

import app.config.Config;
import app.enums.LoginKey;

import java.util.EnumMap;

/**
 * @author pickjob@126.com
 * @date 2019-05-16
 **/
public abstract class BaseController {
    public abstract boolean isNeedLogin();

    public Config loadDefaultConfig() {
        throw new UnsupportedOperationException();
    }

    public void loadWithLoginEnumMap(EnumMap<LoginKey, String> loginEnumMap) {
        throw new UnsupportedOperationException();
    }

    public void loadWithData(Object data) {
        throw new UnsupportedOperationException();
    }
}
