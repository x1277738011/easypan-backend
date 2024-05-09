package com.easypan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto {
    private String registerEmailTitle = "邮箱验证码";

    private String registerEmailContent = "你好，你的邮箱验证码是:%s,15分钟有效";

    private Integer userIninUseSpace = 5;

    public String getRegisterEmailTitle() {
        return registerEmailTitle;
    }

    public void setRegisterEmailTitle(String registerEmailTitle) {
        this.registerEmailTitle = registerEmailTitle;
    }

    public String getRegisterEmailContent() {
        return registerEmailContent;
    }

    public void setRegisterEmailContent(String registerEmailContent) {
        this.registerEmailContent = registerEmailContent;
    }

    public Integer getUserIninUseSpace() {
        return userIninUseSpace;
    }

    public void setUserIninUseSpace(Integer userIninUseSpace) {
        this.userIninUseSpace = userIninUseSpace;
    }
}
