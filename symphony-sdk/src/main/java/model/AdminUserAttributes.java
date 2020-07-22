package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminUserAttributes {
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String displayName;
    private String companyName;
    private String department;
    private String division;
    private String title;
    private String twoFactorAuthPhone;
    private String workPhoneNumber;
    private String mobilePhoneNumber;
    private String accountType;
    private String location;
    private String jobFunction;
    private List<String> assetClasses;
    private List<String> industries;
    private String userName;
    private String smsNumber;
    private UserKey currentKey;
    private UserKey previousKey;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTwoFactorAuthPhone() {
        return twoFactorAuthPhone;
    }

    public void setTwoFactorAuthPhone(String twoFactorAuthPhone) {
        this.twoFactorAuthPhone = twoFactorAuthPhone;
    }

    public String getWorkPhoneNumber() {
        return workPhoneNumber;
    }

    public void setWorkPhoneNumber(String workPhoneNumber) {
        this.workPhoneNumber = workPhoneNumber;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobFunction() {
        return jobFunction;
    }

    public void setJobFunction(String jobFunction) {
        this.jobFunction = jobFunction;
    }

    public List<String> getAssetClasses() {
        return assetClasses;
    }

    public void setAssetClasses(List<String> assetClasses) {
        this.assetClasses = assetClasses;
    }

    public List<String> getIndustries() {
        return industries;
    }

    public void setIndustries(List<String> industries) {
        this.industries = industries;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }

    public UserKey getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(UserKey currentKey) {
        this.currentKey = currentKey;
    }

    public UserKey getPreviousKey() {
        return previousKey;
    }

    public void setPreviousKey(UserKey previousKey) {
        this.previousKey = previousKey;
    }
}
