package com.xiaoxu.config;

import com.jfinal.config.*;
import com.jfinal.template.Engine;

public class YourJFinalConfig extends JFinalConfig {
    @Override
    public void configConstant(Constants constants) {
    }

    @Override
    public void configRoute(Routes routes) {
        routes.scan("com.xiaoxu.controller");
    }

    @Override
    public void configEngine(Engine engine) {

    }

    @Override
    public void configPlugin(Plugins plugins) {

    }

    @Override
    public void configInterceptor(Interceptors interceptors) {

        // todo 添加业务层全局拦截器
//        interceptors.addGlobalServiceInterceptor(new GlobalServiceInterceptor());

    }


    @Override
    public void configHandler(Handlers handlers) {

    }

}