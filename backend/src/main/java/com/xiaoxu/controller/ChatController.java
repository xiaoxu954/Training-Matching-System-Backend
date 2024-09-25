package com.xiaoxu.controller;


import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.xiaoxu.commom.ChatConstant;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.chat.ChatRequest;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.ChatMessageVO;
import com.xiaoxu.model.vo.PrivateMessageVO;
import com.xiaoxu.service.ChatService;
import com.xiaoxu.service.UserService;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.GetRequest;
import io.jboot.web.controller.annotation.PostRequest;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * 聊天控制器
 */
@EnableCORS(allowOrigin = "http://localhost:3002")
@RequestMapping("/api/chat")
@Api(tags = "聊天管理模块")
public class ChatController extends JbootController {
    /**
     * 聊天服务
     */
    @Inject
    private ChatService chatService;

    /**
     * 用户服务
     */
    @Inject
    private UserService userService;

    /**
     * 私聊
     *
     * @param chatRequest 聊天请求
     */
    @PostRequest
    @ApiOperation(value = "获取私聊")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chatRequest", value = "聊天请求"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public void getPrivateChat(@JsonBody ChatRequest chatRequest) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> privateChat = chatService.getPrivateChat(chatRequest, ChatConstant.PRIVATE_CHAT, loginUser);
        renderJson(Ret.ok("data", privateChat));
    }

    /**
     * 团队聊天
     *
     * @param chatRequest 聊天请求
     */
    @PostRequest
    @ApiOperation(value = "获取队伍聊天")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chatRequest", value = "聊天请求"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public void getTeamChat(@JsonBody ChatRequest chatRequest) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> teamChat = chatService.getTeamChat(chatRequest, ChatConstant.TEAM_CHAT, loginUser);
        renderJson(Ret.ok("data", teamChat));
    }

    /**
     * 大厅聊天
     */
    @GetRequest
    @ApiOperation(value = "获取大厅聊天")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public void getHallChat() {
        User loginUser = userService.getLoginUser(getRequest());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<ChatMessageVO> hallChat = chatService.getHallChat(ChatConstant.HALL_CHAT, loginUser);
        renderJson(Ret.ok("data", hallChat));
    }

    @GetRequest
    public void listPrivateChat() {
        List<PrivateMessageVO> privateMessageVOList = chatService.listPrivateChat(getRequest());
        renderJson(Ret.ok("data", privateMessageVOList));
    }
}
