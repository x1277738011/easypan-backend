package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto {
    private String registerMailTitle = "邮箱验证码";

    private String registerMailContent = "你好，你的邮箱验证码是:%s,15分钟有效";

    private Integer userIninUseSpace = 5;

    public String getRegisterMailTitle() {
        return registerMailTitle;
    }

    public void setRegisterMailTitle(String registerMailTitle) {
        this.registerMailTitle = registerMailTitle;
    }

    public String getRegisterMailContent() {
        return registerMailContent;
    }

    public void setRegisterMailContent(String registerMailContent) {
        this.registerMailContent = registerMailContent;
    }

    public Integer getUserIninUseSpace() {
        return userIninUseSpace;
    }

    public void setUserIninUseSpace(Integer userIninUseSpace) {
        this.userIninUseSpace = userIninUseSpace;
    }
}
