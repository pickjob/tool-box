package app.common;

import app.controller.common.BaseController;

import java.util.EnumMap;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-20
 **/
public class Context {
    private EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<LoginKeyEnum, String>(LoginKeyEnum.class);
    // 用于控制流程
    private BaseController nextController;

    public static final Context getInstance() {
        return ContextHolder.INSTANCE;
    }

    public BaseController getNextController() {
        return nextController;
    }

    public void setNextController(BaseController nextController) {
        this.nextController = nextController;
    }

    public EnumMap<LoginKeyEnum, String> getLoginEnumMap() {
        return loginEnumMap;
    }

    public void setLoginEnumMap(EnumMap<LoginKeyEnum, String> loginEnumMap) {
        this.loginEnumMap = loginEnumMap;
    }

    public static enum LoginKeyEnum {
        HOST,
        PORT,
        INDEX,
        ACCOUNT,
        PASSWORD;
    }

    private static class ContextHolder {
        private static final Context INSTANCE = new Context();
    }

    private Context (){}
}