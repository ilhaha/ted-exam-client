package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * 试卷（含题目列表）
 */
public class ExamPaperVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("topicNumber")
    private Long topicNumber;

    @SerializedName("questions")
    private List<QuestionBankWithOptionVO> questions;

    public Long getTopicNumber() {
        return topicNumber;
    }

    public List<QuestionBankWithOptionVO> getQuestions() {
        return questions;
    }
}
