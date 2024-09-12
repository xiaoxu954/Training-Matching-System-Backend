//package com.xiaoxu.validator;
//
//import com.jfinal.core.Controller;
//import com.jfinal.kit.Ret;
//import com.jfinal.validate.Validator;
//
//public class UserValidator extends Validator {
//    @Override
//    protected void validate(Controller controller) {
//        setRet(Ret.fail());
//        setShortCircuit(true); //短验证
//        //全部User方法会走到这
//        //取得方法名
//        String actionMethodName = getActionMethodName();
//        if (actionMethodName.equals("userLogin")) {
////            validateRequired("userAccount", "message", "账号不能为空");
////            validateRequired("userPassword", "message", "密码不能为空");
//
//        } else if (actionMethodName.equals("userRegister")) {
////            validateRequired("userAccount", "message", "账号不能为空");
////            validateRequired("userPassword", "message", "密码不能为空");
////            validateRequired("checkPassword", "message", "确认密码不能为空");
//        }
//    }
//
//    @Override
//    protected void handleError(Controller controller) {
//        //验证失败，返回错误信息
//        controller.renderJson(getRet());
//
//    }
//}
