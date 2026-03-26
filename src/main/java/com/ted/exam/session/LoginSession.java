package com.ted.exam.session;

import com.ted.exam.model.resp.ExamCandidateInfoVO;
import com.ted.exam.model.resp.LoginVO;
import com.ted.exam.model.resp.UserInfoVO;

/**
 * 登录会话存储（单例）
 * 登录成功后调用 {@link #login(LoginVO)} 写入，
 * 任意位置调用 {@link #get()} 读取，
 * 退出时调用 {@link #logout()} 清除。
 */
public class LoginSession {

    private static final LoginSession INSTANCE = new LoginSession();

    private String token;
    private String role;
    private ExamCandidateInfoVO examInfo;
    private UserInfoVO userInfo;

    private LoginSession() {
    }

    public static LoginSession get() {
        return INSTANCE;
    }

    public void login(LoginVO resp) {
        this.token = resp.getToken();
        this.role = resp.getRole();
        this.examInfo = resp.getExamCandidateInfoVO();
    }

    public void logout() {
        this.token = null;
        this.role = null;
        this.examInfo = null;
        this.userInfo = null;
    }

    public void setUserInfo(UserInfoVO userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfoVO getUserInfo() {
        return userInfo;
    }

    /** 用户昵称 */
    public String getNickname() {
        return userInfo != null ? userInfo.getNickname() : null;
    }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public ExamCandidateInfoVO getExamInfo() {
        return examInfo;
    }


    /** 考试计划ID */
    public Long getPlanId() {
        return examInfo != null ? examInfo.getPlanId() : null;
    }

    /** 准考证号 */
    public String getExamNumber() {
        return examInfo != null ? examInfo.getExamNumber() : null;
    }

    /** 考试时间描述 */
    public String getExamTime() {
        return examInfo != null ? examInfo.getExamTime() : null;
    }

    /** 计划名称 */
    public String getPlanName() {
        return examInfo != null ? examInfo.getPlanName() : null;
    }

    /** 考场ID */
    public Long getClassroomId() {
        return examInfo != null ? examInfo.getClassroomId() : null;
    }

    /** 考场名称 */
    public String getClassroomName() {
        return examInfo != null ? examInfo.getClassroomName() : null;
    }

    /** 警示短片路径 */
    public String getWarningShortFilm() {
        return examInfo != null ? examInfo.getWarningShortFilm() : null;
    }

    /** 是否开启违规提醒 */
    public Boolean isProctorWarningEnabled() {
        return examInfo != null ? examInfo.getEnableProctorWarning() : null;
    }

    /** 考试时长（分钟） */
    public Integer getExamDuration() {
        return examInfo != null ? examInfo.getExamDuration() : null;
    }

    /** 二寸照路径 */
    public String getFacePhoto() {
        return examInfo != null ? examInfo.getFacePhoto() : null;
    }
}
