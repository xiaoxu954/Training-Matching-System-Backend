package com.xiaoxu.service.provider;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.MD5;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.commom.UserConstant;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.LoginUserVO;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.UserService;
import com.xiaoxu.utils.AlgorithmUtils;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xiaoxu.commom.UserConstant.USER_LOGIN_STATE;

@Bean
public class UserServiceProvider extends JbootServiceBase<User> implements UserService {
    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "xiaoxu";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
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
            //todo 默认头像
            String defaultAvatar = "https://img.alicdn.com/imgextra/i1/O1CN01EI93PS1xWbnJ87dXX_!!6000000006451-2-tps-150-150.png";
            user.setUserProfile(defaultAvatar);
            this.save(user);
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验
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
    public long addUser(User user) {
        // 1. 校验
        if (StringUtils.isAnyBlank(user.getUserAccount(), user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (user.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (user.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能重复
        long count = findCountByColumns(Columns.create("userAccount", user.getUserAccount()));
        synchronized (user.getUserAccount().intern()) {

            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            // 2. 加密
            String encryptPassword = MD5.create().digestHex(((SALT + user.getUserPassword()).getBytes()));
            // 3. 插入数据
            User addUser = new User();
            addUser.setUserAccount(user.getUserAccount());
            addUser.setUserPassword(encryptPassword);
            this.save(addUser);
            return addUser.getId();
        }
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUserAccount(user.getUserAccount());
        loginUserVO.setUserName(user.getUserName());
        loginUserVO.setUserAvatar(user.getUserAvatar());
        loginUserVO.setUserProfile(user.getUserProfile());
        loginUserVO.setUserRole(user.getUserRole());
        loginUserVO.setTags(user.getTags());
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
        userVO.setTags(user.getTags());
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


    /**
     * 推荐匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Columns queryWrapper = new Columns();

        queryWrapper.isNotNull("tags");
        // todo 校验
//        queryWrapper.select("id", "tags");
        List<User> userList = this.findListByColumns(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下表 => 相似度'
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签的 或当前用户为自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        //按编辑距离有小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //有顺序的userID列表
        List<Long> userListVo = topUserPairList.stream()
                .map(pari -> pari.getKey().getId()).collect(Collectors.toList());

        //根据id查询user完整信息
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Columns userQueryWrapper = new Columns();
        userQueryWrapper.in("id", userListVo);
//        Map<Long, List<User>> userIdUserListMap = this.findListByColumns(userQueryWrapper).stream()
//                .map(user -> getUserVO(user))
//                .collect(Collectors.groupingBy(User::getId));
//
        Map<Long, List<UserVO>> userIdUserListMap = new HashMap<>();
        for (User user : this.findListByColumns(userQueryWrapper)) {
            UserVO userVO = getUserVO(user);
            if (!userIdUserListMap.containsKey(userVO.getId())) {
                userIdUserListMap.put(userVO.getId(), new ArrayList<>());
            }
            userIdUserListMap.get(userVO.getId()).add(userVO);
        }

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userListVo) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }


}
