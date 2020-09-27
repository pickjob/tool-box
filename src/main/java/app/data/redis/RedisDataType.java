package app.data.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public enum RedisDataType {
    STRING,
    LIST,
    SET,
    HASH,
    NONE,
    UNKNOW;

    public static RedisDataType value(String type) {
        if ("string".equals(type)) {
            return STRING;
        } else if ("list".equals(type)) {
            return LIST;
        } else if ("set".equals(type)) {
            return SET;
        } else if ("hash".equals(type)) {
            return HASH;
        } else if ("none".equals(type)) {
            return NONE;
        }
        logger.error("unknow redis type: {}", type);
        return UNKNOW;
    }
    private static final Logger logger = LogManager.getLogger(RedisDataType.class);
}
