package com.xiaoxu;

import io.jboot.app.JbootApplication;

public class Main extends JbootApplication {
    public static void main(String[] args) {
        JbootApplication.setBootArg("jboot.swagger.path", "/swaggerui");
        JbootApplication.setBootArg("jboot.swagger.title", "Jboot API 测试");
        JbootApplication.setBootArg("jboot.swagger.description", "这是一个Jboot对Swagger支持的测试demo。");
        JbootApplication.setBootArg("jboot.swagger.version", "1.0");
        JbootApplication.setBootArg("jboot.swagger.termsOfService", "http://jboot.io");
        JbootApplication.setBootArg("jboot.swagger.contactEmail", "fuhai999@gmail.com");
        JbootApplication.setBootArg("jboot.swagger.contactName", "fuhai999");
        JbootApplication.setBootArg("jboot.swagger.contactUrl", "http://jboot.io");
        JbootApplication.setBootArg("jboot.swagger.host", "127.0.0.1:8081");
        JbootApplication.setBootArg("jboot.swagger.basePackage", "com.xiaoxu.controller");
        JbootApplication.run(args);
    }

}