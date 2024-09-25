package com.xiaoxu.service.provider;

import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.message.MessageQueryRequest;
import com.xiaoxu.model.entity.Blog;
import com.xiaoxu.model.entity.Message;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.InteractionMessageVO;
import com.xiaoxu.model.vo.MessageVO;
import com.xiaoxu.service.BlogService;
import com.xiaoxu.service.MessageService;
import com.xiaoxu.service.UserService;
import io.jboot.aop.annotation.Bean;
import io.jboot.aop.annotation.Lazy;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Bean
@Slf4j
public class MessageServiceProvider extends JbootServiceBase<Message> implements MessageService {


    private UserService userService = new UserServiceProvider();

    @Lazy
    private BlogService blogService = new BlogServiceProvider();


    @Override
    public boolean addStarMessage(Message message) {
        Long fromId = message.getFromId();
        Long toId = message.getToId();
        String text = message.getText();
        Long blogId = message.getBlogId();
        User user = userService.findById(fromId);
        Blog blog = blogService.findById(blogId);
        // 添加消息前，先检查此消息是否存在并且是否未读
        Columns queryWrapper = new Columns();

        Columns eq = queryWrapper.eq("toId", toId).eq("fromId", fromId).eq("blogId", blogId).eq("isRead", 0).eq("type", 1);
        Long count = this.findCountByColumns(eq);

        boolean save = false;
        if (count != null && count < 1) {
            message.setAvatarUrl(user.getUserAvatar());
            message.setText(user.getUserName() + text + blog.getTitle());
            Object save1 = this.save(message);
            save = (boolean) save1;

            if (!save) {
                log.error("用户：{} 收藏：{} 的博客：{} 后，添加收藏消息到消息表失败了！", fromId, toId, blogId);
            }
        }
        return save;
    }

    @Override
    public boolean addLikeMessage(Message message) {
        Long fromId = message.getFromId();
        Long toId = message.getToId();
        String text = message.getText();
        Long blogId = message.getBlogId();
        User user = userService.findById(fromId);
        Blog blog = blogService.findById(blogId);
        // 添加消息前，先检查此消息是否存在并且是否未读
        Columns queryWrapper = new Columns();
        Columns eq = queryWrapper.eq("toId", toId).eq("fromId", fromId).eq("blogId", blogId).eq("isRead", 0).eq("type", 0);
        Long count = this.findCountByColumns(eq);
        boolean save = false;
        if (count != null && count < 1) {
            message.setAvatarUrl(user.getUserAvatar());
            message.setText(user.getUserName() + text + blog.getTitle());
            Object save1 = this.save(message);
            save = (boolean) save1;

            if (!save) {
                log.error("用户：{} 点赞:{} 的博客：{} 后，添加点赞消息到消息表失败了！", fromId, toId, blogId);
            }
        }
        return save;
    }

    @Override
    public boolean addFollowMessage(Message message) {
        Long fromId = message.getFromId();
        Long toId = message.getToId();
        String text = message.getText();
        User user = userService.findById(fromId);
        // 添加消息前，先检查此消息是否存在并且是否未读
        Columns columns = new Columns();
        columns.eq("toId", toId).eq("fromId", fromId).eq("isRead", 0).eq("type", 2);
        Long count = this.findCountByColumns(columns);
        boolean save = false;
        if (count != null && count < 1) {
            message.setAvatarUrl(user.getUserAvatar());
            message.setText(user.getUserName() + text);
            Object save1 = this.save(message);
            save = (boolean) save1;

            if (!save) {
                log.error("用户：{} 关注用户：{} 时发送关注消息失败了！", fromId, toId);
            }
        }
        return save;
    }

    @Override
    public InteractionMessageVO listInteractionMessage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        Columns messageMapper = new Columns();
        Columns starMessageNumQueryWrapper = new Columns();
        starMessageNumQueryWrapper.eq("toId", userId).eq("isRead", 0);
        long starMessageNum = this.findCountByColumns(starMessageNumQueryWrapper);

        Columns likeMessageNumQueryWrapper = new Columns();
        long likeMessageNum = this.findCountByColumns(likeMessageNumQueryWrapper);
        Columns followMessageNumQueryWrapper = new Columns();
        long followMessageNum = this.findCountByColumns(followMessageNumQueryWrapper);


        return new InteractionMessageVO(likeMessageNum, starMessageNum, followMessageNum);
    }

    @Override
    public List<MessageVO> listMessages(MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        Integer type = messageQueryRequest.getType();
        User loginUser = userService.getLoginUser(request);
        if (type == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        Columns queryWrapper = new Columns();

        queryWrapper.eq("toId", loginUser.getId());

        queryWrapper.eq("type", type);
        List<Message> messageList = this.findListByColumns(queryWrapper);
        List<MessageVO> messageVOList = messageList.stream().map(message -> {
            MessageVO messageVO = new MessageVO();

//            BeanUtils.copyProperties(message, messageVO);

            messageVO.setId(message.getId());
            messageVO.setType(message.getType());
            messageVO.setFromId(message.getFromId());
            messageVO.setToId(message.getToId());
            messageVO.setText(message.getText());
            messageVO.setAvatarUrl(message.getAvatarUrl());
            messageVO.setTeamId(message.getTeamId());
            messageVO.setBlogId(message.getBlogId());
            messageVO.setIsRead(message.getIsRead());
            messageVO.setCreateTime(message.getCreateTime());


            return messageVO;
        }).collect(Collectors.toList());
        // 查询的消息变为已读
        List<Message> readedMessageList = messageList.stream().map(message -> {
            Message readedMessage = new Message();
            readedMessage.setId(message.getId());
            readedMessage.setIsRead(1);
            return readedMessage;
        }).collect(Collectors.toList());
        //todo 批量更新
//        boolean b = this.updateBatchById(readedMessageList, 100);
        boolean b = true;
        for (Message message : readedMessageList) {
            b = this.update(message);
        }
        if (!b) {
            log.error("用户：{} 读取消息后将消息改为已读失败了！", loginUser.getId());
        }
        return messageVOList;
    }
}
