package com.xiaoxu.websocket;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.xiaoxu.config.HttpSessionConfig;
import com.xiaoxu.model.dto.message.MessageRequest;
import com.xiaoxu.model.entity.Chat;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.ChatMessageVO;
import com.xiaoxu.model.vo.WebSocketVO;
import com.xiaoxu.service.ChatService;
import com.xiaoxu.service.TeamService;
import com.xiaoxu.service.UserService;
import com.xiaoxu.service.provider.ChatServiceProvider;
import com.xiaoxu.service.provider.TeamServiceProvider;
import com.xiaoxu.service.provider.UserServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.xiaoxu.commom.ChatConstant.PRIVATE_CHAT;
import static com.xiaoxu.commom.ChatConstant.TEAM_CHAT;
import static com.xiaoxu.commom.UserConstant.ADMIN_ROLE;

/**
 * 导入web Socket服务端组件web Socket Server用于和客户端通信
 */
@Slf4j
@ServerEndpoint(value = "/api/websocket/{userId}/{teamId}/myapp.ws", configurator = HttpSessionConfig.class)
//@ServerEndpoint("/ws/{teamId}")
public class WebSocketServer {

    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, WebSocketServer>> ROOMS = new HashMap<>();

    /**
     * 线程安全的无序的集合
     */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * 会话池
     */
    private static final Map<String, Session> SESSION_POOL = new HashMap<>(0);

    /**
     * 用户服务
     */
    private static UserService userService = new UserServiceProvider();

    /**
     * 聊天服务
     */
    private static ChatService chatService = new ChatServiceProvider();

    /**
     * 团队服务
     */
    private static TeamService teamService = new TeamServiceProvider();


    /**
     * 房间在线人数
     */
    private static int onlineCount = 0;


    /**
     * 当前信息
     */
    private Session session;

    /**
     * http会话
     */
    private HttpSession httpSession;

    /**
     * 上网数
     *
     * @return int
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 添加在线计数
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * 子在线计数
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    @OnOpen
    public void opOpen(Session session,
                       @PathParam(value = "userId") String userId,
                       @PathParam("teamId") String teamId) {
        System.err.println("客户端：" + teamId + "建立连接");
        SESSION_POOL.put(userId, session);
        SESSION_POOL.put(teamId, session);

        if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
            sendError(userId, "参数有误");
            return;
        }
        this.session = session;
        if (!"NaN".equals(teamId)) {
            if (!ROOMS.containsKey(teamId)) {
                ConcurrentHashMap<String, WebSocketServer> room = new ConcurrentHashMap<>(0);
                room.put(userId, this);
                ROOMS.put(String.valueOf(teamId), room);
                addOnlineCount();
            } else if (!((ConcurrentHashMap) ROOMS.get(teamId)).containsKey(userId)) {
                ((ConcurrentHashMap) ROOMS.get(teamId)).put(userId, this);
                addOnlineCount();
            }
        } else {
            SESSIONS.add(session);
            SESSION_POOL.put(userId, session);
//            this.sendAllUsers();
        }

    }

    @OnClose
    public void onClose(@PathParam("userId") String userId,
                        @PathParam("teamId") String teamId,
                        Session session) {
        try {
            if (!"NaN".equals(teamId)) {
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0) {
                    subOnlineCount();
                }
            } else {
                if (!SESSION_POOL.isEmpty()) {
                    SESSION_POOL.remove(userId);
                    SESSIONS.remove(session);
                }
//                sendAllUsers();
            }
        } catch (Exception e) {
            log.error("exception message", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param userId
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            return;
        }
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        Long toId = messageRequest.getToId();
        Long teamId = messageRequest.getTeamId();
        String text = messageRequest.getText();
        Integer chatType = messageRequest.getChatType();

        User fromUser = userService.findById(userId);
        Team team = teamService.findById(teamId);


        if (chatType == PRIVATE_CHAT) {
            // 私聊
            privateChat(fromUser, toId, text, chatType);
        } else if (chatType == TEAM_CHAT) {
            // 队伍内聊天
            teamChat(fromUser, text, team, chatType);
        } else {
            // 群聊
            hallChat(fromUser, text, chatType);
        }
        System.err.println("收到来自客户端" + userId + "的消息：" + message);
    }

    /**
     * 发送一个消息
     *
     * @param userId  用户编号
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        Session userSession = SESSION_POOL.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                synchronized (userSession) {
                    userSession.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }


    /**
     * 队伍聊天
     *
     * @param user     用户
     * @param text     文本
     * @param team     团队
     * @param chatType 聊天类型
     */
    private void teamChat(User user, String text, Team team, Integer chatType) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        WebSocketVO fromWebSocketVO = new WebSocketVO();

        fromWebSocketVO.setId(user.getId());
        fromWebSocketVO.setUsername(user.getUserName());
        fromWebSocketVO.setUserAccount(user.getUserAccount());
        fromWebSocketVO.setAvatarUrl(user.getUserAvatar());


        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setTeamId(team.getId());
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        if (Objects.equals(user.getId(), team.getUserId()) || user.getUserRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
//        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
//        if (Objects.equals(loginUser.getId(), user.getId())) {
//            chatMessageVo.setIsMy(true);
//        }
        chatMessageVo.setIsMy(true);
        String toJson = new Gson().toJson(chatMessageVo);
        try {
            broadcast(String.valueOf(team.getId()), toJson);
            saveChat(user.getId(), null, text, team.getId(), chatType);
//            chatService.deleteKey(CACHE_CHAT_TEAM, String.valueOf(team.getId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 队伍内群发消息
     *
     * @param teamId 团队id
     * @param msg    消息
     */
    public static void broadcast(String teamId, String msg) {
        ConcurrentHashMap<String, WebSocketServer> map = ROOMS.get(teamId);
        // keySet获取map集合key的集合  然后在遍历key即可
        for (String key : map.keySet()) {
            try {
                WebSocketServer webSocket = map.get(key);
                webSocket.sendMessage(msg);
                System.out.println("webSocket = " + webSocket);
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @throws IOException ioexception
     */
//    @OnMessage
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 大厅聊天
     *
     * @param user     用户
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void hallChat(User user, String text, Integer chatType) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        WebSocketVO fromWebSocketVO = new WebSocketVO();
//        BeanUtils.copyProperties(user, fromWebSocketVO);
        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        if (user.getUserRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
//        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
//        if (Objects.equals(loginUser.getId(), user.getId())) {
//            chatMessageVo.setIsMy(true);
//        }
        chatMessageVo.setIsMy(true);
        String toJson = new Gson().toJson(chatMessageVo);
        sendAllMessage(toJson);
        saveChat(user.getId(), null, text, null, chatType);
    }


    private void privateChat(User user, Long toId, String text, Integer chatType) {
        ChatMessageVO chatMessageVo = chatService
                .chatResult(user.getId(), toId, text, chatType, DateUtil.date(System.currentTimeMillis()));
//        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
//        if (Objects.equals(loginUser.getId(), user.getId())) {
//            chatMessageVo.setIsMy(true);
//        }
        chatMessageVo.setIsMy(true);


        String toJson = new Gson().toJson(chatMessageVo);
        sendOneMessage(toId.toString(), toJson);
        saveChat(user.getId(), toId, text, null, chatType);
    }

    /**
     * 保存聊天
     *
     * @param userId   用户id
     * @param toId     为id
     * @param text     文本
     * @param teamId   团队id
     * @param chatType 聊天类型
     */
    private void saveChat(Long userId, Long toId, String text, Long teamId, Integer chatType) {
//        if (chatType == PRIVATE_CHAT) {
//            User user = userService.findById(userId);
//
//            Set<Long> userIds = stringJsonListToLongSet(user.getFriendIds());
//
//            if (!userIds.contains(toId)) {
//                sendError(String.valueOf(userId), "该用户不是你的好友");
//                return;
//            }
//        }
        Chat chat = new Chat();
        chat.setFromId(userId);
        chat.setText(String.valueOf(text));
        chat.setChatType(chatType);
        chat.setCreateTime(new Date());
        if (toId != null && toId > 0) {
            chat.setToId(toId);
        }
        if (teamId != null && teamId > 0) {
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }


    /**
     * 给所有用户
     */
    public void sendAllUsers() {
        HashMap<String, List<WebSocketVO>> stringListHashMap = new HashMap<>(0);
        List<WebSocketVO> webSocketVos = new ArrayList<>();
        stringListHashMap.put("users", webSocketVos);
        for (Serializable key : SESSION_POOL.keySet()) {
            User user = userService.findById(key);
            WebSocketVO webSocketVO = new WebSocketVO();
            webSocketVO.setId(user.getId());
            webSocketVO.setUsername(user.getUserName());
            webSocketVO.setUserAccount(user.getUserAccount());
            webSocketVO.setAvatarUrl(user.getUserAvatar());
            webSocketVos.add(webSocketVO);
        }
        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap));
    }

    /**
     * 广播
     *
     * @param message
     */
    public void sendAllMessage(String message) {
        Collection<Session> sessions = SESSION_POOL.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 发送失败
     *
     * @param userId       用户id
     * @param errorMessage 错误消息
     */
    private void sendError(String userId, String errorMessage) {
        JSONObject obj = new JSONObject();
        obj.set("error", errorMessage);
        sendOneMessage(userId, obj.toString());
    }

}