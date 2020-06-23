package app.common;

import app.controller.common.BaseController;
import app.data.redis.RedisData;
import javafx.stage.Stage;

import java.security.Key;
import java.util.EnumMap;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-20
 **/
public class Context {
    private EnumMap<LoginKeyEnum, String> loginEnumMap = new EnumMap<LoginKeyEnum, String>(LoginKeyEnum.class);
    private Stage mainStage;

    // 用于控制流程
    private BaseController nextController;

    public static final Context getInstance() {
        return ContextHolder.INSTANCE;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public BaseController getNextController() {
        return nextController;
    }

    public void setNextController(BaseController nextController) {
        this.nextController = nextController;
    }

    public String getLoginEnum(LoginKeyEnum keyEnum) {
        return loginEnumMap.get(keyEnum);
    }

    public void setLoginEnum(LoginKeyEnum keyEnum, String value) {
        this.loginEnumMap.put(keyEnum, value);
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