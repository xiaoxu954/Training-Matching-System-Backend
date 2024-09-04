package com.xiaoxu.controller;

import cn.hutool.crypto.digest.MD5;
import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.commom.UserConstant;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.user.*;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.LoginUserVO;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.UserService;
import io.jboot.db.model.Columns;
import io.jboot.support.jwt.EnableJwt;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.xiaoxu.commom.ErrorCode.PARAMS_ERROR;
import static com.xiaoxu.service.provider.UserServiceProvider.SALT;


@EnableCORS
@EnableJwt
@RequestMapping("/user")
@Api("用户相关API")
public class UserController extends JbootController {

    @Inject
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "用户注册")
    public void userRegister(@JsonBody UserRegisterRequest userRegisterRequest) {

        if (userRegisterRequest == null) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }

        String result = userService.userRegister(userAccount, userPassword, checkPassword);
        renderJson(Ret.ok("msg", result));
    }

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录", notes = "用户登录")
    public void userLogin(@JsonBody UserLoginRequest userLoginRequest) {
        System.out.println("userLoginRequest = " + userLoginRequest);
        if (userLoginRequest == null) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, getRequest());
        renderJson(Ret.ok("data", loginUserVO));
    }


    // region 增删改查


    @ApiOperation(value = "添加用户", notes = "添加用户")
    public void addUser(@JsonBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = new User();
        user.setUserName(userAddRequest.getUserName());
        user.setUserAccount(userAddRequest.getUserAccount());
        user.setUserAvatar(userAddRequest.getUserAvatar());
        user.setUserRole(userAddRequest.getUserRole());

        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = MD5.create().digestHex(((SALT + defaultPassword).getBytes()));

        user.setUserPassword(encryptPassword);
        Object save = userService.save(user);
        renderJson(Ret.ok("data", save));
    }

    /**
     * 删除用户
     */
    @ApiOperation(value = "删除用户", notes = "删除用户")
    public void deleteUser(Integer id) {
        boolean result = userService.deleteById(id);
        if (result) {
            renderJson(Ret.ok("data", result));
        } else {
            renderJson(Ret.fail("data", result));
        }
    }

    /**
     * 更新用户
     */
    @ApiOperation(value = "更新用户", notes = "更新用户")
    public void updateUser(@JsonBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            renderJson(Ret.fail("data", false).set("msg", PARAMS_ERROR));
        }
        Long id = userUpdateRequest.getId();
        User updateUser = userService.findById(id);

        updateUser.setUserName(userUpdateRequest.getUserName());
        updateUser.setUserAvatar(userUpdateRequest.getUserAvatar());
        updateUser.setUserProfile(userUpdateRequest.getUserProfile());
        updateUser.setUserRole(userUpdateRequest.getUserRole());
        boolean result = userService.update(updateUser);

        if (result) {
            renderJson(Ret.ok("data", true));
        } else {
            renderJson(Ret.fail("msg", "更新失败"));
        }
    }

    /**
     * 更新个人信息
     */
    public void updateMy(@JsonBody UserUpdateMyRequest userUpdateMyRequest) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        User user = new User();

        user.setUserName(userUpdateMyRequest.getUserName());
        user.setUserAvatar(userUpdateMyRequest.getUserAvatar());
        user.setUserProfile(userUpdateMyRequest.getUserProfile());

        user.setId(loginUser.getId());
        boolean result = userService.update(user);
        renderJson(Ret.ok("data", result));
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @return
     */
    public void getUserById(long id) {
        if (id <= 0) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser.getUserRole() != UserConstant.ADMIN_ROLE) {
            renderJson(Ret.fail("msg", "无权限"));
        } else {
            User user = userService.findById(id);
            renderJson(Ret.ok("data", user));
        }

    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @return
     */
    public void getUserVoById(long id) {
        if (id <= 0) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }
        User user = userService.findById(id);
        UserVO userVO = userService.getUserVO(user);

        renderJson(Ret.ok("data", userVO));


    }


    /**
     * 根据条件查询用户
     *
     * @param columns
     */
    @ApiOperation(value = "根据条件查询用户", notes = "根据条件查询用户")
    public void findUserByColumns(Columns columns) {
        User firstByColumns = userService.findFirstByColumns(columns);
        renderJson(Ret.ok("data", firstByColumns));

    }

    /**
     * 查询所有用户
     */
    @ApiOperation(value = "查询所有用户", notes = "查询所有用户")
    public void findAllUser() {
        List<User> all = userService.findAll();
        renderJson(Ret.ok("data", all));
    }


    /**
     * 分页查询用户
     */
    @ApiOperation(value = "分页查询用户", notes = "分页查询用户")
    public void findUserByPage(Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            renderJson(Ret.fail("msg", PARAMS_ERROR));
        }
        Page<User> userPage = userService.paginate(page, pageSize);
        renderJson(Ret.ok("data", userPage));

    }


}
