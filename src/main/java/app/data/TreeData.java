package app.data;

/**
 * @author pickjob@126.com
 * @date 2020-09-27
 */
public abstract class TreeData {
    private String name;
    private String canonicalName;
    private Boolean mocked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public Boolean getMocked() {
        return mocked;
    }

    public void setMocked(Boolean mocked) {
        this.mocked = mocked;
    }
}
