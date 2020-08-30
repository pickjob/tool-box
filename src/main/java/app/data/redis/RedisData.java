package app.data.redis;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-22
 **/
public class RedisData {
    private String key;
    private String canonicalKey;
    private Object value;
    private Long ttl;
    private RedisDataType type;

    public RedisData(String key, String canonicalKey) {
        this.key = key;
        this.canonicalKey = canonicalKey;
        this.type = RedisDataType.NONE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCanonicalKey() {
        return canonicalKey;
    }

    public void setCanonicalKey(String canonicalKey) {
        this.canonicalKey = canonicalKey;
    }

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