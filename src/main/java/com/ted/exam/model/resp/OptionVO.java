package com.ted.exam.model.resp;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 题目选项
 */
public class OptionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    /**
     * 选项标识（如 A、B、C、D）
     */
    private String optionKey;

    @SerializedName("question")
    private String question;

    /**
     * 选项内容（用于显示，等同于question）
     */
    @SerializedName("optionValue")
    private String optionValue;

    @SerializedName("attachment")
    private String attachment;

    @SerializedName("questionBankId")
    private Long questionBankId;

    @SerializedName("isCorrectAnswer")
    private Boolean isCorrectAnswer;

    public Long getId() {
        return id;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptionValue() {
        // 优先使用optionValue，否则使用question
        return optionValue != null ? optionValue : question;
    }

    public String getAttachment() {
        return attachment;
    }

    public Long getQuestionBankId() {
        return questionBankId;
    }

    public Boolean getIsCorrectAnswer() {
        return isCorrectAnswer;
    }
}
