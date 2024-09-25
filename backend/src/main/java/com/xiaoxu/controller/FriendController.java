package com.xiaoxu.controller;


import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.friend.FriendAddRequest;
import com.xiaoxu.model.dto.friend.FriendQueryRequest;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.FriendService;
import com.xiaoxu.service.UserService;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.GetRequest;
import io.jboot.web.controller.annotation.PostRequest;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@EnableCORS(allowOrigin = "http://localhost:3002")
@Slf4j
@RequestMapping("/api/friend")
public class FriendController extends JbootController {

    @Inject
    private UserService userService;

    @Inject
    private FriendService friendService;

    @PostRequest
    public void addFriend(@JsonBody FriendAddRequest friendAddRequest) {
        Long friendId = friendAddRequest.getFriendId();
        if (friendId == null || friendId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long userId = loginUser.getId();
        boolean result = friendService.addFriend(userId, friendId);
        renderJson(Ret.ok("data", result));
    }

    @GetRequest
    public void listFriends() {
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        List<UserVO> friendList = friendService.listFriends(userId, getRequest());
        renderJson(Ret.ok("data", friendList));
    }

    @PostRequest
    public void searchFriends(@JsonBody FriendQueryRequest friendQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        if (friendQueryRequest == null || StringUtils.isBlank(friendQueryRequest.getSearchParam())) {
            throw new BusinessException(ErrorCode.MESSAGE_NULL);
        }
        List<UserVO> userVOS = friendService.searchFriends(friendQueryRequest, userId);
        renderJson(Ret.ok("data", userVOS));
    }
}
