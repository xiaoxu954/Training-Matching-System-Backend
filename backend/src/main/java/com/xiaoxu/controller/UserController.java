package com.xiaoxu.controller;

import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.user.UserLoginRequest;
import com.xiaoxu.model.dto.user.UserRegisterRequest;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.LoginUserVO;
import com.xiaoxu.service.UserService;
import io.jboot.db.model.Columns;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.PostRequest;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@EnableCORS
@RequestMapping("/user")
@Api(description = "用户相关接口文档", basePath = "/swaggerui", tags = "abc")
public class UserController extends JbootController {

    @Inject
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "用户注册")
    public void userRegister(@JsonBody() UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            renderJson(Ret.fail("参数为空"));
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        renderJson(Ret.ok("data", result));
    }

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录", notes = "用户登录")
    public void userLogin(@JsonBody UserLoginRequest userLoginRequest) {
        System.out.println("userLoginRequest = " + userLoginRequest);
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, getRequest());
        renderJson(Ret.ok("data", loginUserVO));
    }


    // region 增删改查



    @ApiOperation(value = "添加用户", notes = "添加用户")
    @PostRequest
    public void add(User user) {
//        User user = getModel(User.class);
        Object save = userService.save(user);
        renderJson(Ret.ok("data", save));
    }

    /**
     * 删除用户
     */
    @ApiOperation(value = "删除用户", notes = "删除用户")
    public void delete() {
        String id = getPara("id");
        boolean result = userService.deleteById(id);
        System.out.println("result = " + result);
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
    public void update() {
        User user = getModel(User.class);
        user.getUserAccount();
        user.getAvatarUrl();
        String email = user.getEmail();
        Long id = user.getId();
        // 查询id值 的User将其name属性改为James并更新到数据库
        User updateUser = userService.findById(id);
        updateUser.set("email", email);
        userService.update(user);
        renderJson(Ret.ok("data", updateUser.getId()));
    }

    /**
     * 根据 id 获取用户（管理员）
     *
     * @param id
     * @return
     */
    public void getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.findById(id);
        Integer userRole = user.getUserRole();
        if (userRole == 1) {
            renderJson(Ret.ok("data", user));
        } else {
            renderJson(Ret.fail("data", "非管理员"));
        }
    }


    /**
     * 根据条件查询用户
     *
     * @param columns
     */
    @ApiOperation(value = "根据条件查询用户", notes = "根据条件查询用户")
    public void findFirstByColumns(Columns columns) {
        User firstByColumns = userService.findFirstByColumns(columns);
        renderJson(Ret.ok("data", firstByColumns));

    }

    /**
     * 查询所有用户
     */
    @ApiOperation(value = "查询所有用户", notes = "查询所有用户")
    public void findAll() {
        List<User> all = userService.findAll();
        System.out.println("all = " + all);
        renderJson(Ret.ok("data", all));
    }


    /**
     * 分页查询用户
     */
    @ApiOperation(value = "分页查询用户", notes = "分页查询用户")
    public void findUserByPage() {
        int page = Integer.parseInt(getPara("page"));
        int pageSize = Integer.parseInt(getPara("pageSize"));
        Page<User> userPage = userService.paginate(page, pageSize);
        renderJson(Ret.ok("data", userPage));

    }

}
