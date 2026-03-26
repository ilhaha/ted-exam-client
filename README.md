# 北京市特种设备作业人员考试系统-考试端

基于Java Swing开发的考试客户端系统，支持Windows XP及以上操作系统。

## 技术栈

- Java 8 (兼容Windows XP/7/10/11)
- Swing GUI框架
- Maven构建

## 系统要求

- Windows XP SP3 及以上
- JRE 8 或更高版本
- 最低分辨率: 1024x768

## 项目结构

```
ted-exam-client/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── ted/
│       │           └── exam/
│       │               ├── Main.java
│       │               ├── ui/
│       │               │   ├── LoginFrame.java
│       │               │   └── components/
│       │               │       └── RoundedButton.java
│       │               └── util/
│       │                   └── ThemeUtil.java
│       └── resources/
│           └── images/
└── README.md
```

## 构建和运行

### 构建
```bash
mvn clean package
```

### 运行
```bash
java -jar target/ted-exam-client-1.0.jar
```

## 功能特性

### 登录页面
- 用户名/密码输入
- 记住密码功能
- 登录按钮
- 考试端标识

### UI特性
- 扁平化现代设计
- 适配高DPI屏幕
- 经典Windows风格按钮
