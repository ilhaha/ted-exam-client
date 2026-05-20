package com.ted.exam.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ted.exam.model.req.ExamRecordReq;
import com.ted.exam.model.req.LoginReq;
import com.ted.exam.model.common.ApiResponse;
import com.ted.exam.model.resp.ExamPaperVO;
import com.ted.exam.model.resp.LoginVO;
import com.ted.exam.model.resp.UserInfoVO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;

/**
 * API 工具类
 */
public class ApiUtil {
        private static String BASE_URL = "http://localhost:8000";
//    阿里云
//    private static String BASE_URL = "http://120.79.7.238:8000";
//    定福庄内网
//    private static String BASE_URL = "http://172.16.188.190:8000";
//    定福庄外网
//    private static String BASE_URL = "http://211.100.254.202:8000";
//    考试中心
//    private static String BASE_URL = "http://172.16.1.7:8000";
    private static String authToken;
    private static final Gson GSON = new GsonBuilder().create();

    static {
    }

    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void clearAuthToken() {
        authToken = null;
    }

    private static String doGet(String path) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Accept", "application/json");
            if (authToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return sb.toString();
            } else {
                return "{\"code\":" + code + "}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String doGet(String path, String... params) {
        HttpURLConnection conn = null;
        try {
            StringBuilder urlWithParams = new StringBuilder(BASE_URL).append(path);
            if (params.length > 0) {
                urlWithParams.append("?username=")
                        .append(URLEncoder.encode(params[0], "UTF-8"));
            }
            URL url = new URL(urlWithParams.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Accept", "application/json");
            if (authToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return sb.toString();
            } else {
                return "{\"code\":" + code + "}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String doPost(String path, String body) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            if (authToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return sb.toString();
            } else {
                return "{\"code\":" + code + "}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 身份证号正则校验
     */
    public static boolean isValidIdCardFormat(String id) {
        return id != null && id.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    }

    /**
     * Base64 编码
     */
    public static String encodeByBase64(String txt) {
        return Base64.getEncoder().encodeToString(txt.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 解码
     */
    public static String decodeByBase64(String txt) {
        return new String(Base64.getDecoder().decode(txt), StandardCharsets.UTF_8);
    }

    /**
     * MD5 加密
     */
    public static String encryptByMd5(String txt) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(txt.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return txt;
        }
    }

    /**
     * RSA 加密（使用后端提供的公钥）
     */
    public static String encryptByRsa(String txt) {
        try {
            String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM51dgYtMyF+tTQt80sfFOpSV27a7t9u"
                    + "aUVeFrdGiVxscuizE7H8SMntYqfn9lp8a5GH5P1/GGehVjUD2gF/4kcCAwEAAQ==";

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(txt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("RSA 加密失败: " + e.getMessage());
            return txt;
        }
    }

    /**
     * AES 加密
     */
    public static String encryptByAes(String word) {
        return encryptByAes(word, "XwKsGlMcdPMEhR1B");
    }

    /**
     * AES 加密（指定密钥）
     */
    public static String encryptByAes(String word, String keyWord) {
        try {
            byte[] key = keyWord.getBytes(StandardCharsets.UTF_8);
            if (key.length < 16) {
                byte[] paddedKey = new byte[16];
                System.arraycopy(key, 0, paddedKey, 0, key.length);
                key = paddedKey;
            } else if (key.length > 16) {
                byte[] truncatedKey = new byte[16];
                System.arraycopy(key, 0, truncatedKey, 0, 16);
                key = truncatedKey;
            }

            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(key, "AES");
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(word.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("AES 加密失败: " + e.getMessage());
            return word;
        }
    }

    /**
     * 根据身份证号获取准考证列表
     * @param encryptedId 加密后的身份证号
     * @return 准考证选项列表，失败返回空列表
     * @throws RuntimeException 当接口返回错误时，异常信息为错误提示
     */
    public static List<ApiResponse.ExamNumberOption> getExamNumbersByIdCard(String encryptedId) throws RuntimeException {
        String resp = doGet("/exam/examPlan/examNumbers", encryptedId);

        if (resp == null) {
            throw new RuntimeException("网络请求失败，请检查网络连接");
        }

        try {
            ApiResponse<List<ApiResponse.ExamNumberOption>> result =
                    GSON.fromJson(resp, new TypeToken<ApiResponse<List<ApiResponse.ExamNumberOption>>>(){}.getType());

            if (result == null) {
                throw new RuntimeException("响应解析失败");
            }
            if (!result.isOk()) {
                throw new RuntimeException(result.getErrorMsg());
            }
            return result.getData() != null ? result.getData() : new ArrayList<ApiResponse.ExamNumberOption>();
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("响应格式错误");
        }
    }

    /**
     * 获取考生信息
     */
    public static String getCandidatesInfo() {
        return doGet("/exam/examPlan/candidates/info");
    }

    /**
     * 获取当前用户信息
     * @return UserInfoVO，失败返回 null
     */
    public static UserInfoVO getUserInfo() {
        String resp = doGet("/auth/user/info");
        if (resp == null) {
            return null;
        }
        try {
            ApiResponse<UserInfoVO> result = GSON.fromJson(resp,
                    new TypeToken<ApiResponse<UserInfoVO>>() {}.getType());
            if (result != null && result.isOk()) {
                return result.getData();
            }
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 考试结束修改考试计划状态
     * @param id 考试计划ID
     */
    public static String endExam(int id) {
        return doPost("/exam/examPlan/end/" + id, "{}");
    }

    /**
     * 提交登录
     * @param encryptedId 加密后的身份证号
     * @param encryptedExamNumber 加密后的准考证号
     * @param encryptedPassword 加密后的密码
     * @return 登录响应（调用方自行判断 result.isOk()）
     * @throws RuntimeException 网络或解析异常
     */
    public static LoginVO submitLogin(String encryptedId, String encryptedExamNumber, String encryptedPassword) throws RuntimeException {
        String body = GSON.toJson(new LoginReq(encryptedId, encryptedExamNumber, encryptedPassword, true, "ACCOUNT"));
        String resp = doPost("/auth/exam/login", body);

        if (resp == null) {
            throw new RuntimeException("网络请求失败，请检查网络连接");
        }

        try {
            ApiResponse<LoginVO> result = GSON.fromJson(resp,
                    new TypeToken<ApiResponse<LoginVO>>() {}.getType());
            if (result == null) {
                throw new RuntimeException("响应解析失败");
            }
            if (!result.isOk()) {
                throw new RuntimeException(result.getErrorMsg());
            }
            LoginVO loginResp = result.getData();
            if (loginResp != null && loginResp.getToken() != null) {
                authToken = loginResp.getToken();
            }
            return loginResp;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("响应格式错误");
        }
    }

    /**
     * 获取考试计划的题目
     * @param planId 考试计划ID
     * @param userId 用户ID
     * @return ExamPaperVO，失败返回 null
     */
    public static ExamPaperVO getExamPaper(long planId, long userId) {
        String resp = doGet("/examconnect/questionBank/candidate/paper/" + planId + "/" + userId);
        if (resp == null) {
            return null;
        }
        try {
            ApiResponse<ExamPaperVO> result = GSON.fromJson(resp,
                    new TypeToken<ApiResponse<ExamPaperVO>>() {}.getType());
            if (result != null && result.isOk()) {
                return result.getData();
            }
        } catch (Exception e) {
            System.err.println("获取试卷失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 退出登录
     */
    public static void logout() {
        doPost("/auth/logout", "{}");
    }

    /**
     * 提交考试记录
     * @param req 考试记录请求
     * @return 是否提交成功
     */
    public static boolean submitExamRecord(ExamRecordReq req) throws RuntimeException {
        String body = GSON.toJson(req);
        String resp = doPost("/exam/examRecords/candidates/add", body);

        if (resp == null) {
            throw new RuntimeException("网络请求失败，请检查网络连接");
        }

        try {
            ApiResponse<Object> result = GSON.fromJson(resp,
                    new TypeToken<ApiResponse<Object>>() {}.getType());
            if (result == null) {
                throw new RuntimeException("响应解析失败");
            }
            if (!result.isOk()) {
                throw new RuntimeException(result.getErrorMsg());
            }
            return true;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("响应格式错误");
        }
    }
}
