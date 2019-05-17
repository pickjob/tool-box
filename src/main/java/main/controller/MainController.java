package main.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pickjob@126.com
 * @time 2019-05-16
 **/
public class MainController {
    private static final Logger logger = LogManager.getLogger(MainController.class);

    public void init() {
        logger.info("init ...");
    }
}
