//package com.xiaoxu.config;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//public class CorsFilter implements Filter {
//
//    private String allowedOrigin; // 用于存储允许的域名
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        // 初始化参数可以从web.xml或Spring的配置中读取
//        allowedOrigin = "http://9g05996g09.zicp.fun"; // 替换为你实际允许的域名
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // 设置允许跨域的域名
//        httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigin);
//
//        // 设置允许的方法
//        httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
//
//        // 允许发送Cookie
//        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
//
//        // 设置允许的Header
//        httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with,Authorization,content-type");
//
//        // 预检请求的有效期
//        httpResponse.setHeader("Access-Control-Max-Age", "3600");
//
//        chain.doFilter(request, response);
//    }
//
//    @Override
//    public void destroy() {
//        // 清理资源
//    }
//}