package com.xiaoxu.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.jfinal.core.paragetter.Para;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;

@EnableCORS
@RequestMapping("/captcha")
public class CaptchaController extends JbootController {

    static LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(400, 100, 4, 20);//指定（宽，高,验证码数量，干扰项数量）

    public void getImage() {
        //生成带有直线干扰的验证码图片
        lineCaptcha = CaptchaUtil.createLineCaptcha(400, 100, 4, 20);//指定（宽，高,验证码数量，干扰项数量）
        //获取验证码的信息
        System.out.println(lineCaptcha.getCode());//验证码的文本信息，用于校对
        //将验证码图片转换为base64格式字符串
        System.out.println(lineCaptcha.getImageBase64());//不带数据格式前缀
        renderJson(Ret.ok("img", lineCaptcha.getImageBase64()));
    }

    public void checkCode(@Para("code") String code) {
        boolean result = lineCaptcha.verify(code);//校验用户输入的验证码,result为true表示验证码正确
        if (result) {
            renderJson(Ret.ok("message", "验证码正确"));
        } else {
            renderJson(Ret.fail("message", "验证码错误"));
        }
    }

}
