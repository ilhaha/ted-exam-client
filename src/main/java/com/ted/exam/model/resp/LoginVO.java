package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 登录响应
 */
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("token")
    private String token;

    @SerializedName("role")
    private String role;

    @SerializedName("examCandidateInfoVO")
    private ExamCandidateInfoVO examCandidateInfoVO;

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public ExamCandidateInfoVO getExamCandidateInfoVO() {
        return examCandidateInfoVO;
    }
}
