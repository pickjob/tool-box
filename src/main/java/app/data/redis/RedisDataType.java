package app.data.redis;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public enum RedisDataType {
    STRING,
    HASH,
    NONE,
    UNKNOW;

    public static String toString(RedisDataType type) {
        if (type == STRING) {
            return "string";
        } else if (type == HASH) {
            return "hash";
        } else if (type == UNKNOW) {
            return "unknow";
        }
        return "";
    }

    public static RedisDataType value(String type) {
        if ("string".equals(type)) {
            return STRING;
        } else if ("hash".equals(type)) {
            return HASH;
        } else if ("none".equals(type)) {
            return NONE;
        }
        return UNKNOW;
    }
}
