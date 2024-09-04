package com.xiaoxu.service.provider;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.MD5;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.LoginUserVO;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.UserService;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xiaoxu.commom.UserConstant.USER_LOGIN_STATE;

@Bean
public class UserServiceProvider extends JbootServiceBase<User> implements UserService {
    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "xiaoxu";

    @Override
    public String userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {

            return ErrorCode.MESSAGE_NULL.getMessage();
        }
        if (userAccount.length() < 4) {
            return ErrorCode.ACCOUNT_TOO_SHIRT.getMessage();
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            return ErrorCode.PASSWORD_TOO_SHIRT.getMessage();
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return ErrorCode.TWO_PASSWORD_NOT_SAME.getMessage();
        }
        synchronized (userAccount.intern()) {

            // 账户不能重复
            long count = findCountByColumns(Columns.create("userAccount", userAccount));

            if (count > 0) {
                return ErrorCode.ACCOUNT_EXIST.getMessage();
            }

            // 2. 加密
            String encryptPassword = MD5.create().digestHex(((SALT + userPassword).getBytes()));
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            this.save(user);
            return user.getId().toString();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = MD5.create().digestHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        User dataUser = DAO.findFirstByColumns(Columns.create("userAccount", userAccount).eq("userPassword", encryptPassword));

        // 用户不存在
        if (dataUser == null) {
//            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, dataUser);
        return this.getLoginUserVO(dataUser);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUserName(user.getUserName());
        loginUserVO.setUserAvatar(user.getUserAvatar());
        loginUserVO.setUserProfile(user.getUserProfile());
        loginUserVO.setUserRole(user.getUserRole());
        loginUserVO.setCreateTime(user.getCreateTime());
        loginUserVO.setUpdateTime(user.getUpdateTime());
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUserName(user.getUserName());
        userVO.setUserProfile(user.getUserProfile());
        userVO.setUserAvatar(user.getUserAvatar());
        userVO.setUserRole(user.getUserRole());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {

        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.findById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
