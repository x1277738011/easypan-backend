package com.easypan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    @Value("${spring.mail.username}")
    private String sendUserName;
    @Value("${admin.emails}")
    private String adminEmails;
    @Value("${project.folder}")
    private String projectFolder;

    // qq登录
    @Value("${qq.app.id:}")
    private String qqAppId;
    @Value("${qq.app.key:}")
    private String qqAppKey;
    @Value("${qq.url.authorization:}")
    private String qqUrlAuthorization;
    @Value("${qq.url.access.token:}")
    private String qqUrlAccessToken;
    @Value("${qq.url.openid:}")
    private String qqUrlOpenId;
    @Value("${qq.url.user.info:}")
    private String qqUrlUserInfo;
    @Value("${qq.url.redirect:}")
    private String qqUrlRedirect;

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    public void setAdminEmails(String adminEmails) {
        this.adminEmails = adminEmails;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public String getQqAppId() {
        return qqAppId;
    }

    public void setQqAppId(String qqAppId) {
        this.qqAppId = qqAppId;
    }

    public String getQqAppKey() {
        return qqAppKey;
    }

    public void setQqAppKey(String qqAppKey) {
        this.qqAppKey = qqAppKey;
    }

    public String getQqUrlAuthorization() {
        return qqUrlAuthorization;
    }

    public void setQqUrlAuthorization(String qqUrlAuthorization) {
        this.qqUrlAuthorization = qqUrlAuthorization;
    }

    public String getQqUrlAccessToken() {
        return qqUrlAccessToken;
    }

    public void setQqUrlAccessToken(String qqUrlAccessToken) {
        this.qqUrlAccessToken = qqUrlAccessToken;
    }

    public String getQqUrlOpenId() {
        return qqUrlOpenId;
    }

    public void setQqUrlOpenId(String qqUrlOpenId) {
        this.qqUrlOpenId = qqUrlOpenId;
    }

    public String getQqUrlUserInfo() {
        return qqUrlUserInfo;
    }

    public void setQqUrlUserInfo(String qqUrlUserInfo) {
        this.qqUrlUserInfo = qqUrlUserInfo;
    }

    public String getQqUrlRedirect() {
        return qqUrlRedirect;
    }

    public void setQqUrlRedirect(String qqUrlRedirect) {
        this.qqUrlRedirect = qqUrlRedirect;
    }
}
