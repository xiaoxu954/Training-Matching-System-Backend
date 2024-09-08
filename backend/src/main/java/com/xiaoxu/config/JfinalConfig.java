package com.xiaoxu.config;

import com.jfinal.config.*;
import com.jfinal.template.Engine;

public class JfinalConfig extends JFinalConfig {
    @Override
    public void configConstant(Constants constants) {

    }

    @Override
    public void configRoute(Routes routes) {

    }

    @Override
    public void configEngine(Engine engine) {

    }

    @Override
    public void configPlugin(Plugins plugins) {

    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
//        interceptors.add(new SessionInViewInterceptor());

        //自定义异常拦截器
        interceptors.add(new ExceptionInterceptor());


    }

    @Override
    public void configHandler(Handlers handlers) {

    }
}
