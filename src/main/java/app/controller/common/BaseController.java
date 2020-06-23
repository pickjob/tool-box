package app.controller.common;

import app.common.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author pickjob@126.com
 * @time 2019-05-16
 **/
public class BaseController {
    private static final Logger logger = LogManager.getLogger(BaseController.class);
    private boolean needLogin;
    protected Context context;

    public void init(Context context) {
        logger.info("init ...");
        this.context = context;
    }

    public void run(){};

    public boolean isNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }
}
