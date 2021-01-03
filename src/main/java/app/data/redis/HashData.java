package app.data.redis;

/**
 * @author: pickjob@126.com
 * @date: 2020-06-19
 **/
public class HashData {
    private String key;
    private String value;

    public HashData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
