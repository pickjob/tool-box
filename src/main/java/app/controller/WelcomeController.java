package app.controller;

import app.controller.common.BaseController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: pickjob@126.com
 * @date: 2020-04-20
 **/
public class WelcomeController extends BaseController {
    private static final Logger logger = LogManager.getLogger(WelcomeController.class);

    @Override
    public boolean isNeedLogin() {
        return false;
    }
}
