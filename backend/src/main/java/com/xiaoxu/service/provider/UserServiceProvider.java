package com.xiaoxu.service.provider;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.MD5;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.commom.UserConstant;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {

            // 账户不能重复
            long count = findCountByColumns(Columns.create("userAccount", userAccount));

            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, dataUser);
        // 4. 返回结果
        LoginUserVO loginUserVO = this.getLoginUserVO(dataUser);
        return loginUserVO;
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
        userVO.setUserAccount(user.getUserAccount());
        userVO.setUserProfile(user.getUserProfile());
        userVO.setUserAvatar(user.getUserAvatar());
        userVO.setUserRole(user.getUserRole());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
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

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }
}
