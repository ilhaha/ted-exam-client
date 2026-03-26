package com.ted.exam.model.req;

import com.google.gson.annotations.SerializedName;

/**
 * API 统一请求包装类
 */
public class LoginReq {
    @SerializedName("username")
    private String username;

    @SerializedName("examNumber")
    private String examNumber;

    @SerializedName("password")
    private String password;

    @SerializedName("rememberMe")
    private boolean rememberMe;

    @SerializedName("authType")
    private String authType;

    @SerializedName("clientId")
    private String clientId = "ef51c9a3e9046c4f2ea45142c8a8344a";

    public LoginReq() {
    }

    public LoginReq(String username, String examNumber, String password, boolean rememberMe, String authType) {
        this.username = username;
        this.examNumber = examNumber;
        this.password = password;
        this.rememberMe = rememberMe;
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExamNumber() {
        return examNumber;
    }

    public void setExamNumber(String examNumber) {
        this.examNumber = examNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}
