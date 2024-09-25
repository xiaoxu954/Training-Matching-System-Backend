package com.xiaoxu.service.provider;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.chat.ChatRequest;
import com.xiaoxu.model.entity.Chat;
import com.xiaoxu.model.entity.Friend;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.ChatMessageVO;
import com.xiaoxu.model.vo.PrivateMessageVO;
import com.xiaoxu.model.vo.WebSocketVO;
import com.xiaoxu.service.*;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.xiaoxu.commom.UserConstant.ADMIN_ROLE;


@Bean
public class ChatServiceProvider extends JbootServiceBase<Chat> implements ChatService {


    private UserService userService = new UserServiceProvider();

    private TeamService teamService = new TeamServiceProvider();

    private FriendService friendService = new FriendServiceProvider();

    private UserTeamService userTeamService = new UserTeamServiceProvider();

    /**
     * 获取私人聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     */
    @Override
    public List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long toId = chatRequest.getToId();
        if (toId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        Columns chatLambdaQueryWrapper = new Columns();
//        chatLambdaQueryWrapper.eq("fromId", loginUser.getId()).or().eq("toId", loginUser.getId());

        Columns queryWrapper = new Columns();
        queryWrapper.eq("fromId", loginUser.getId())
                .eq("toId", toId)
                .eq("chatType", chatType)
                .or()
                .eq("fromId", toId)
                .eq("toId", loginUser.getId())
                .eq("chatType", chatType);

//        chatLambdaQueryWrapper.
//                and(privateChat -> privateChat.eq(Chat::getFromId, loginUser.getId()).eq(Chat::getToId, toId)
//                        .or().
//                        eq(Chat::getToId, loginUser.getId()).eq(Chat::getFromId, toId)
//                ).eq(Chat::getChatType, chatType);

        // 两方共有聊天
        List<Chat> list = this.findListByColumns(queryWrapper);
        List<ChatMessageVO> chatMessageVOList = list.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(loginUser.getId(),
                    toId, chat.getText(), chatType,
                    chat.getCreateTime());
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            return chatMessageVo;
        }).collect(Collectors.toList());
        System.out.println(chatMessageVOList);
        return chatMessageVOList;
    }


    /**
     * 聊天结果
     *
     * @param userId 用户id
     * @param text   文本
     * @return {@link ChatMessageVO}
     */
    private ChatMessageVO chatResult(Long userId, String text) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.findById(userId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
//        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        fromWebSocketVo.setId(fromUser.getId());
        fromWebSocketVo.setUsername(fromUser.getUserName());
        fromWebSocketVo.setUserAccount(fromUser.getUserAccount());
        fromWebSocketVo.setAvatarUrl(fromUser.getUserAvatar());

        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        return chatMessageVo;
    }

    /**
     * 聊天结果
     *
     * @param userId     用户id
     * @param toId       到id
     * @param text       文本
     * @param chatType   聊天类型
     * @param createTime 创建时间
     * @return {@link ChatMessageVO}
     */
    @Override
    public ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.findById(userId);
        User toUser = userService.findById(toId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
        WebSocketVO toWebSocketVo = new WebSocketVO();
//        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        fromWebSocketVo.setId(fromUser.getId());
        fromWebSocketVo.setUsername(fromUser.getUserName());
        fromWebSocketVo.setUserAccount(fromUser.getUserAccount());
        fromWebSocketVo.setAvatarUrl(fromUser.getUserAvatar());


//        BeanUtils.copyProperties(toUser, toWebSocketVo);
        toWebSocketVo.setId(toUser.getId());
        toWebSocketVo.setUsername(toUser.getUserName());
        toWebSocketVo.setUserAccount(toUser.getUserAccount());
        toWebSocketVo.setAvatarUrl(toUser.getUserAvatar());


        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setToUser(toWebSocketVo);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setText(text);
        chatMessageVo.setCreateTime(DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss"));
        return chatMessageVo;
    }


    /**
     * 获取团队聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    @Override
    public List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long teamId = chatRequest.getTeamId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }

        // 判断用户是否在队伍中
        if (!userTeamService.teamHasUser(teamId, loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您还未加入此队伍");
        }
        Team team = teamService.findById(teamId);
        Columns chatLambdaQueryWrapper = new Columns();

        chatLambdaQueryWrapper.eq("chatType", chatType).eq("teamId", teamId);

        List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, team.getUserId(), chatLambdaQueryWrapper);
        return chatMessageVOS;
    }

    /**
     * 获取大厅聊天
     *
     * @param chatType  聊天类型
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public List<ChatMessageVO> getHallChat(int chatType, User loginUser) {

        Columns chatQueryWrapper = new Columns();
        chatQueryWrapper.eq("chatType", chatType);
        return returnMessage(loginUser, null, chatQueryWrapper);
    }

    @Override
    public List<PrivateMessageVO> listPrivateChat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        Columns columns = new Columns();
        Columns eq = columns.eq("userId", userId);
        List<Long> frinedIdList = friendService.findListByColumns(eq)
                .stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(frinedIdList)) {
            return new ArrayList<>();
        }

        List<Chat> privateMessageVOList = this.getLastPrivateChatMessages(userId, frinedIdList);

        return privateMessageVOList.stream().map(chat -> {
            PrivateMessageVO privateMessageVO = new PrivateMessageVO();
            privateMessageVO.setId(chat.getId());
            privateMessageVO.setText(chat.getText());
            privateMessageVO.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
            Long fromId = chat.getFromId();
            Long toId = chat.getToId();
            long friendId = 0L;
            if (fromId != userId) {
                friendId = fromId;
            }
            if (toId != userId) {
                friendId = toId;
            }
            privateMessageVO.setFriendId(friendId);
            User friend = userService.findById(friendId);
            privateMessageVO.setAvatarUrl(friend.getUserAvatar());
            privateMessageVO.setUsername(friend.getUserName());
            return privateMessageVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Chat> getLastPrivateChatMessages(Long userId, List<Long> friendIdList) {

        //todo  完善sql语句   --获得最后聊天记录
        String sql = "SELECT c.* FROM chat c JOIN (SELECT MAX(id) AS max_id " +
                "FROM chat WHERE isDelete = 0 AND (fromId = ? OR toId = ?)" +
                " AND toId IN  GROUP BY toId) AS max_ids ON c.id = max_ids.max_id AND " +
                "(c.fromId = ? OR c.toId = ?) ORDER BY createTime DESC;";
//        getDao().getSql(sql, userId, userId, friendIdList, userId, userId).findList(Chat.class);
        return Collections.emptyList();
    }

    /**
     * 检查是否是我的消息
     */
    private List<ChatMessageVO> checkIsMyMessage(User loginUser, List<ChatMessageVO> chatRecords) {
        return chatRecords.stream().peek(chat -> {
            if (chat.getFromUser().getId() != loginUser.getId() && chat.getIsMy()) {
                chat.setIsMy(false);
            }
            if (chat.getFromUser().getId() == loginUser.getId() && !chat.getIsMy()) {
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 返回消息
     *
     * @param loginUser              登录用户
     * @param userId                 用户id
     * @param chatLambdaQueryWrapper 聊天lambda查询包装器
     */
    private List<ChatMessageVO> returnMessage(User loginUser,
                                              Long userId,
                                              Columns chatLambdaQueryWrapper) {
        List<Chat> chatList = this.findListByColumns(chatLambdaQueryWrapper);
        return chatList.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(chat.getFromId(), chat.getText());
            boolean isCaptain = userId != null && userId.equals(chat.getFromId());
            if (userService.findById(chat.getFromId()).getUserRole() == ADMIN_ROLE || isCaptain) {
                chatMessageVo.setIsAdmin(true);
            }
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            chatMessageVo.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
            return chatMessageVo;
        }).collect(Collectors.toList());
    }
}
