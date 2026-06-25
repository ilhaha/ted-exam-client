package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 题目（含选项）
 */
public class QuestionBankWithOptionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("knowledgeTypeTopicNumber")
    private Long knowledgeTypeTopicNumber;

    @SerializedName("knowledgeTypeId")
    private Long knowledgeTypeId;

    @SerializedName("knowledgeTypeName")
    private String knowledgeTypeName;

    @SerializedName("id")
    private Long id;

    @SerializedName("question")
    private String question;

    @SerializedName("attachment")
    private String attachment;

    @SerializedName("questionType")
    private Integer questionType;

    @SerializedName("points")
    private Integer points;

    @SerializedName("options")
    private List<OptionVO> options;

    public Long getKnowledgeTypeTopicNumber() {
        return knowledgeTypeTopicNumber;
    }

    public Long getKnowledgeTypeId() {
        return knowledgeTypeId;
    }

    public String getKnowledgeTypeName() {
        return knowledgeTypeName;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAttachment() {
        return attachment;
    }

    public Integer getQuestionType() {
        return questionType;
    }

    public Integer getPoints() {
        return points;
    }

    public int getPointsOrDefault() {
        return points != null ? points : 1;
    }

    public List<OptionVO> getOptions() {
        return options;
    }

    /** 题目类型：1=单选题 2=判断题 3=多选题 */
    public String getQuestionTypeName() {
        if (questionType == null) return "未知";
        switch (questionType) {
            case 0: return "单选题";
            case 1: return "判断题";
            case 2: return "多选题";
            default: return "未知";
        }
    }
}
