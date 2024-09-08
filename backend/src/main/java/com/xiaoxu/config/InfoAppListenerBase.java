package com.xiaoxu.config;

import com.jfinal.config.Interceptors;
import com.jfinal.config.Routes;
import io.jboot.aop.jfinal.JfinalHandlers;
import io.jboot.core.listener.JbootAppListenerBase;

public class InfoAppListenerBase extends JbootAppListenerBase {
    @Override
    public void onRouteConfig(Routes routes) {
        super.onRouteConfig(routes);
    }

    @Override
    public void onInterceptorConfig(Interceptors interceptors) {
        interceptors.addGlobalActionInterceptor(new ExceptionInterceptor());
        super.onInterceptorConfig(interceptors);

    }

    @Override
    public void onHandlerConfig(JfinalHandlers handlers) {
        super.onHandlerConfig(handlers);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStartFinish() {
        super.onStartFinish();
    }
}
