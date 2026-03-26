package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 登录成功后返回的考生考试信息
 */
public class ExamCandidateInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("planId")
    private Long planId;

    @SerializedName("examNumber")
    private String examNumber;

    @SerializedName("examTime")
    private String examTime;

    @SerializedName("planName")
    private String planName;

    @SerializedName("classroomId")
    private Long classroomId;

    @SerializedName("classroomName")
    private String classroomName;

    @SerializedName("warningShortFilm")
    private String warningShortFilm;

    @SerializedName("enableProctorWarning")
    private Boolean enableProctorWarning;

    @SerializedName("examDuration")
    private Integer examDuration;

    @SerializedName("facePhoto")
    private String facePhoto;

    public Long getPlanId() {
        return planId;
    }

    public String getExamNumber() {
        return examNumber;
    }

    public String getExamTime() {
        return examTime;
    }

    public String getPlanName() {
        return planName;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public String getWarningShortFilm() {
        return warningShortFilm;
    }

    public Boolean getEnableProctorWarning() {
        return enableProctorWarning;
    }

    public Integer getExamDuration() {
        return examDuration;
    }

    public String getFacePhoto() {
        return facePhoto;
    }
}
