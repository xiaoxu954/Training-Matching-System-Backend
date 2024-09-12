package com.xiaoxu.controller;

import cn.hutool.crypto.digest.MD5;
import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.commom.DeleteRequest;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.exception.ThrowUtils;
import com.xiaoxu.model.dto.user.*;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.LoginUserVO;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.UserService;
import io.jboot.db.model.Columns;
import io.jboot.support.swagger.ParamType;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.GetRequest;
import io.jboot.web.controller.annotation.PostRequest;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.xiaoxu.commom.ErrorCode.PARAMS_ERROR;
import static com.xiaoxu.service.provider.UserServiceProvider.SALT;

//@EnableJwt
//@Before(UserValidator.class)
@EnableCORS(allowOrigin = "http://localhost:3002")
@RequestMapping("/api/user")
@Api(description = "用户相关API", tags = "用户接口")
public class UserController extends JbootController {

    @Inject
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     */
    @PostRequest
    @ApiOperation(value = "用户注册", httpMethod = "Post", notes = "用户注册")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "userAccount", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "userPassword", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "checkPassword", paramType = ParamType.FORM, dataType = "string", required = true)})
    public void userRegister(@JsonBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            renderJson(Ret.fail("message", PARAMS_ERROR));
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            renderJson(Ret.fail("message", PARAMS_ERROR));
        }

        long userId = userService.userRegister(userAccount, userPassword, checkPassword);

        renderJson(Ret.ok("data", userId));
    }

    /**
     * 用户登录
     */
    @PostRequest
    @ApiOperation(value = "用户登录", httpMethod = "Post", notes = "用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userAccount", value = "用户账户", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userPassword", value = "用户密码", paramType = ParamType.FORM, dataType = "string", required = true)})

    public void userLogin(@JsonBody UserLoginRequest userLoginRequest) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, getRequest());
        renderJson(Ret.ok("data", loginUserVO));
    }


    // region 增删改查

    @PostRequest
    @ApiOperation(value = "添加用户", httpMethod = "Post", notes = "添加用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "userAccount", value = "用户账户", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userPassword", value = "用户密码", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userAvatar", value = "用户头像", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userRole", value = "用户角色", paramType = ParamType.FORM, dataType = "string", required = true)})
    public void addUser(@JsonBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = new User();
        if (userAddRequest.getUserAccount() == null) {
            String defaultAvatar = "https://img.alicdn.com/imgextra/i1/O1CN01EI93PS1xWbnJ87dXX_!!6000000006451-2-tps-150-150.png";
            user.setUserProfile(defaultAvatar);
        } else {
            user.setUserProfile(userAddRequest.getUserProfile());
        }
        user.setUserName(userAddRequest.getUserName());
        user.setUserAccount(userAddRequest.getUserAccount());
        user.setUserRole(userAddRequest.getUserRole());
        user.setUserAvatar(userAddRequest.getUserAvatar());
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = MD5.create().digestHex(((SALT + defaultPassword).getBytes()));
        user.setUserPassword(encryptPassword);
        Object save = userService.addUser(user);

        renderJson(Ret.ok("data", save));
    }

    /**
     * 删除用户
     */
    @PostRequest
    @ApiOperation(value = "删除用户", httpMethod = "Post", notes = "删除用户")
    @ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.QUERY, dataType = "int", required = true)
    public void deleteUser(@JsonBody DeleteRequest DeleteRequest) {
        boolean result = userService.deleteById(DeleteRequest.getId());
        if (result) {
            renderJson(Ret.ok("data", result));
        } else {
            renderJson(Ret.fail("data", result));
        }
    }

    /**
     * 更新用户
     */
    @PostRequest
    @ApiOperation(value = "更新用户", httpMethod = "Post", notes = "更新用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.FORM, dataType = "int", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userAvatar", value = "用户头像", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userProfile", value = "用户简介", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userRole", value = "用户角色", paramType = ParamType.FORM, dataType = "string", required = true)})
    public void updateUser(@JsonBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            renderJson(Ret.fail("data", false).set("message", PARAMS_ERROR));
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
            renderJson(Ret.fail("message", "更新失败"));
        }
    }

    /**
     * 更新个人信息
     */
    @PostRequest
    @ApiOperation(value = "更新个人信息", httpMethod = "Post", notes = "更新个人信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userAvatar", value = "用户头像", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(name = "userProfile", value = "用户简介", paramType = ParamType.FORM, dataType = "string", required = true)})
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
    @GetRequest
    @ApiOperation(value = "根据 id 获取用户（仅管理员）", httpMethod = "Get", notes = "根据 id 获取用户（仅管理员）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.QUERY, dataType = "int", required = true)})
    public void getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.findById(id);
        User loginUser = userService.getLoginUser(getRequest());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        renderJson(Ret.ok("message", user));

    }

    /**
     * 根据 id 获取用户（脱敏）
     *
     * @param id
     * @return
     */
    @GetRequest
    @ApiOperation(value = "根据 id 获取用户（脱敏）", httpMethod = "Get", notes = "根据 id 获取用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.QUERY, dataType = "int", required = true)})
    public void getUserVoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.findById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        renderJson(Ret.ok("data", (userService.getUserVO(user))));
    }


    /**
     * 根据条件查询用户
     *
     * @param columns
     */
    @PostRequest
    @ApiOperation(value = "根据条件查询用户", httpMethod = "Post", notes = "根据条件查询用户")
    public void findUserByColumns(Columns columns) {
        User firstByColumns = userService.findFirstByColumns(columns);
        renderJson(Ret.ok("data", firstByColumns));

    }

    /**
     * 查询所有用户
     */
    @GetRequest
    @ApiOperation(value = "查询所有用户", httpMethod = "Get", notes = "查询所有用户")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.QUERY, dataType = "int", required = true))
    public void findAllUser() {
        List<User> all = userService.findAll();
        renderJson(Ret.ok("data", all));
    }


    /**
     * 分页查询用户
     */
    @PostRequest
    @ApiOperation(value = "分页查询用户", httpMethod = "Post", notes = "分页查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", paramType = ParamType.QUERY, dataType = "int", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页大小", paramType = ParamType.QUERY, dataType = "int", required = true),
            @ApiImplicitParam(name = "id", value = "用户id", paramType = ParamType.QUERY, dataType = "int", required = true),
            @ApiImplicitParam(name = "userAccount", value = "用户账号", paramType = ParamType.QUERY, dataType = "String", required = true),
            @ApiImplicitParam(name = "userName", value = "用户昵称", paramType = ParamType.QUERY, dataType = "String", required = true),
            @ApiImplicitParam(name = "userProfile", value = "用户简介", paramType = ParamType.QUERY, dataType = "String", required = true),
            @ApiImplicitParam(name = "userAvatar", value = "用户头像", paramType = ParamType.QUERY, dataType = "String", required = true),
    })

    public void findUserByPage(@JsonBody UserPageRequest userPageRequest) {
        int current = userPageRequest.getCurrent();
        int pageSize = userPageRequest.getPageSize();
        Columns columns = new Columns();
        columns.eq("id", userPageRequest.getId());
        columns.eq("userAccount", userPageRequest.getUserAccount());
        columns.eq("userName", userPageRequest.getUserName());
        columns.eq("userProfile", userPageRequest.getUserProfile());
        columns.eq("userRole", userPageRequest.getUserRole());
        Page<User> userPage = userService.paginateByColumns(current, pageSize, columns);
        renderJson(Ret.ok("data", userPage));
    }

    public void findUserVoByPage(@JsonBody UserPageRequest userPageRequest) {
        int current = userPageRequest.getCurrent();
        int pageSize = userPageRequest.getPageSize();
        Columns columns = new Columns();
        columns.eq("id", userPageRequest.getId());
        columns.eq("userAccount", userPageRequest.getUserAccount());
        columns.eq("userName", userPageRequest.getUserName());
        columns.eq("userProfile", userPageRequest.getUserProfile());
        Page<User> userPage = userService.paginateByColumns(current, pageSize, columns);
        List<UserVO> userVO = userService.getUserVO(userPage.getList());
        renderJson(Ret.ok("data", userVO));
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetRequest
    @ApiOperation(value = "获取当前登录用户", httpMethod = "Get", notes = "获取当前登录用户")
    public void getLoginUser() {

        User user = userService.getLoginUser(getRequest());
        renderJson(Ret.ok("data", userService.getLoginUserVO(user)));
    }

    /**
     * 用户注销
     */
    @PostRequest
    @ApiOperation(value = "注销当前登录用户", httpMethod = "Post", notes = "退出当前登录用户")
    public void userLogout() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        renderJson(Ret.ok("data", result));
    }
}
