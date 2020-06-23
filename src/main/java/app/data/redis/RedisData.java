package app.data.redis;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public class RedisData<T> {
    private String key;
    private T value;
    private Long ttl;
    private RedisDataType type;

    public RedisData(String key) {
        this.key = key;
        this.type = RedisDataType.NONE;
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

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public void setType(RedisDataType type) {
        this.type = type;
    }
}