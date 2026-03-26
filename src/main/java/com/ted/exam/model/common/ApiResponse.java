package com.ted.exam.model.common;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * API 统一响应包装类
 */
public class ApiResponse<T> {
    @SerializedName("code")
    private String code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("success")
    private Boolean success;

    @SerializedName("data")
    private T data;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public boolean isOk() {
        return "0".equals(code) || "200".equals(code) || Boolean.TRUE.equals(success);
    }

    public String getErrorMsg() {
        if (msg != null && !msg.isEmpty()) {
            return msg;
        }
        if (success != null && !success) {
            return "请求失败";
        }
        return "未知错误";
    }

    /**
     * 准考证子项（二级：准考证号）
     */
    public static class ExamNumberItem {
        @SerializedName("value")
        private Object value;

        @SerializedName("label")
        private String label;

        @SerializedName("children")
        private Object children;

        public Object getValue() {
            return value;
        }

        public String getValueAsString() {
            if (value == null) return null;
            return value.toString();
        }

        public String getLabel() {
            return label;
        }

        public ExamNumberItem(Object value, String label, Object children) {
            this.value = value;
            this.label = label;
            this.children = children;
        }

        public ExamNumberItem() {
        }
    }

    /**
     * 考试计划选项（一级：考场/场次）
     */
    public static class ExamNumberOption {
        @SerializedName("value")
        private Integer value;

        @SerializedName("label")
        private String label;

        @SerializedName("children")
        private List<ExamNumberItem> children;

        public Integer getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public List<ExamNumberItem> getChildren() {
            return children;
        }

        public ExamNumberOption(Integer value, String label, List<ExamNumberItem> children) {
            this.value = value;
            this.label = label;
            this.children = children;
        }

        public ExamNumberOption() {
        }
    }
}
