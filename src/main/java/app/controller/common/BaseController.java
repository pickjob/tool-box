package app.controller.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pickjob@126.com
 * @time 2019-05-16
 **/
public abstract class BaseController {
    private static final Logger logger = LogManager.getLogger(BaseController.class);

    abstract public void runWith(Object arg);

    public boolean isNeedLogin() {
        return false;
    }
}
