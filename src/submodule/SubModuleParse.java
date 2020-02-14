package submodule;

import org.apache.commons.lang.StringUtils;

public class SubModuleParse {
    public String module;
    public String path;
    public String url;
    public String branch;

    /**
     * 是否可用
     *
     * @return
     */
    public boolean isAvailable() {
        return !StringUtils.isEmpty(path) && !StringUtils.isEmpty(url) && !StringUtils.isEmpty(branch);
    }

    @Override
    public String toString() {
        return path + "-" + url + "-" + "branch";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        SubModuleParse moduleParse=new SubModuleParse();
        moduleParse.module=module;
        moduleParse.path=path;
        moduleParse.url=url;
        moduleParse.branch=branch;
        return super.clone();
    }
}
