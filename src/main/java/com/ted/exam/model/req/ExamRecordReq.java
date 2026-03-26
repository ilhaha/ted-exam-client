package com.ted.exam.model.req;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 提交考试记录请求
 */
public class ExamRecordReq {

    @SerializedName("planId")
    private Long planId;

    @SerializedName("candidateId")
    private Long candidateId;

    @SerializedName("registrationProgress")
    private Integer registrationProgress;

    @SerializedName("examScores")
    private Integer examScores;

    @SerializedName("reviewStatus")
    private Integer reviewStatus;

    @SerializedName("examPaper")
    private String examPaper;

    @SerializedName("violationType")
    private Integer violationType;

    @SerializedName("violationScreenshots")
    private List<String> violationScreenshots;

    public ExamRecordReq() {
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getRegistrationProgress() {
        return registrationProgress;
    }

    public void setRegistrationProgress(Integer registrationProgress) {
        this.registrationProgress = registrationProgress;
    }

    public Integer getExamScores() {
        return examScores;
    }

    public void setExamScores(Integer examScores) {
        this.examScores = examScores;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getExamPaper() {
        return examPaper;
    }

    public void setExamPaper(String examPaper) {
        this.examPaper = examPaper;
    }

    public Integer getViolationType() {
        return violationType;
    }

    public void setViolationType(Integer violationType) {
        this.violationType = violationType;
    }

    public List<String> getViolationScreenshots() {
        return violationScreenshots;
    }

    public void setViolationScreenshots(List<String> violationScreenshots) {
        this.violationScreenshots = violationScreenshots;
    }
}
