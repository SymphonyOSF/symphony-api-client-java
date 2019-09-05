package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public class ApplicationEntitlement {

    private String appId;
    private String appName;
    private Boolean listed;
    private Boolean install;
    private List<ApplicationProduct> products;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Boolean getListed() {
        return listed;
    }

    public void setListed(Boolean listed) {
        this.listed = listed;
    }

    public Boolean getInstall() {
        return install;
    }

    public void setInstall(Boolean install) {
        this.install = install;
    }

    public List<ApplicationProduct> getProducts() {
        return products;
    }

    public void setProducts(List<ApplicationProduct> products) {
        this.products = products;
    }
}
