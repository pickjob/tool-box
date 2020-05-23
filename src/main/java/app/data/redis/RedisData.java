package app.data.redis;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public class RedisData<T> {
    private String key;
    private T value;
    private RedisDataType type;
    private Map<String, RedisData<?>> children;

    public RedisData(String key) {
        this.key = key;
        this.type = RedisDataType.NONE;
        this.children = new TreeMap<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public RedisDataType getType() {
        return type;
    }

    public void setType(RedisDataType type) {
        this.type = type;
    }

    public Map<String, RedisData<?>> getChildren() {
        return children;
    }

    public void setChildren(Map<String, RedisData<?>> children) {
        this.children = children;
    }
}