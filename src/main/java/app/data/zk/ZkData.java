package app.data.zk;

import app.data.TreeData;

/**
 * @Author pickjob@126.com
 * @Date 2020-09-24
 */
public class ZkData extends TreeData {
    private String value;
    private ZkDataType type;
    private Integer version;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ZkDataType getType() {
        return type;
    }

    public void setType(ZkDataType type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
