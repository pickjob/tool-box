package app.controller.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pickjob@126.com
 * @time 2019-05-16
 **/
public class BaseController {
    private static final Logger logger = LogManager.getLogger(BaseController.class);
    private boolean needLogin;
    protected Object env;

    public void buildUIComponents() {
        // first run after controller instance construct
    }

    public void init() {
        // init controller after buildUIComponents
    }

    public boolean isNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public void setEnv(Object env) {
        this.env = env;
    }
}
