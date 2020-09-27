package app.data.zk;

import app.data.TreeData;

/**
 * @Author ws@yuan-mai.com
 * @Date 2020-09-24
 */
public class ZkData extends TreeData {
    private String value;
    private Integer version;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
