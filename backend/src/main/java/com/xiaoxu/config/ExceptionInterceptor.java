package com.xiaoxu.config;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.core.JFinal;
import com.jfinal.kit.Ret;
import com.xiaoxu.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理拦截器
 */
public class ExceptionInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(ExceptionInterceptor.class);

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        HttpServletRequest request = controller.getRequest();
        try {
            inv.invoke();
        } catch (Exception e) {
            //输入到日记文件
            doLog(inv, e);
            if (e instanceof BusinessException) {
                Ret ret = this.handleServiceException((BusinessException) e);
                controller.renderJson(ret);
            } else {
                inv.invoke();
            }
        }
    }

    private void doLog(Invocation ai, Exception e) {
        //开发模式
        if (JFinal.me().getConstants().getDevMode()) {
            e.printStackTrace();
        }
        //业务异常不记录
        //if( e instanceof NormalException) return;
        StringBuilder sb = new StringBuilder("\n---全局异常处理拦截---\n");
        sb.append("Controller:").append(ai.getController().getClass().getName()).append("\n");
        sb.append("Method:").append(ai.getMethodName()).append("\n");
        sb.append("Exception Type:").append(e.getClass().getName()).append("\n");
        sb.append("Exception Details:");
        log.error(sb.toString(), e);
    }

    public Ret handleServiceException(BusinessException e) {
        log.error(e.getMessage());
        Integer code = e.getCode();
        String message = e.getMessage();

        return Ret.fail("message", message).set("code", code);
    }

}