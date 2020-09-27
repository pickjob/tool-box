package app.data.redis;

import app.data.TreeData;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public class RedisData extends TreeData {
    private Object value;
    private Long ttl;
    private RedisDataType type;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public RedisDataType getType() {
        return type;
    }

    public void setType(RedisDataType type) {
        this.type = type;
    }
}