package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 用户信息响应
 */
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("username")
    private String username;

    @SerializedName("phone")
    private String phone;

    @SerializedName("facePhoto")
    private String facePhoto;

    @SerializedName("avatar")
    private String avatar;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }



    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFacePhoto() {
        return facePhoto;
    }

    public void setFacePhoto(String facePhoto) {
        this.facePhoto = facePhoto;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
