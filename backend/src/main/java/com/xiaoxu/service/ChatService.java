package com.xiaoxu.service;


import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.model.dto.chat.ChatRequest;
import com.xiaoxu.model.entity.Chat;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.ChatMessageVO;
import com.xiaoxu.model.vo.PrivateMessageVO;
import io.jboot.db.model.Columns;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public interface ChatService {

    /**
     * 根据主键查找Model
     *
     * @param id
     * @return
     */
    public Chat findById(Object id);


    /**
     * 根据 Columns 查找单条数据
     *
     * @param columns
     * @return
     */
    public Chat findFirstByColumns(Columns columns);

    /**
     * 根据 Columns 查找单条数据
     *
     * @param columns
     * @param orderBy
     * @return
     */
    public Chat findFirstByColumns(Columns columns, String orderBy);


    /**
     * 查找全部数据
     *
     * @return
     */
    public List<Chat> findAll();


    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @return
     */
    public List<Chat> findListByColumns(Columns columns);


    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param orderBy
     * @return
     */
    public List<Chat> findListByColumns(Columns columns, String orderBy);

    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param count
     * @return
     */
    public List<Chat> findListByColumns(Columns columns, Integer count);

    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param orderBy
     * @param count
     * @return
     */
    public List<Chat> findListByColumns(Columns columns, String orderBy, Integer count);


    /**
     * 根据提交查询数据量
     *
     * @param columns
     * @return
     */
    public long findCountByColumns(Columns columns);


    /**
     * 根据ID 删除model
     *
     * @param id
     * @return
     */
    public boolean deleteById(Object id);


    /**
     * 删除
     *
     * @param model
     * @return
     */
    public boolean delete(Chat model);


    /**
     * 根据 多个 id 批量删除
     *
     * @param ids
     * @return
     */
    public boolean batchDeleteByIds(Object... ids);


    /**
     * 保存到数据库
     *
     * @param model
     * @return id if success
     */
    public Object save(Chat model);


    /**
     * 保存或更新
     *
     * @param model
     * @return id if success
     */
    public Object saveOrUpdate(Chat model);

    /**
     * 更新
     *
     * @param model
     * @return
     */
    public boolean update(Chat model);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<Chat> paginate(int page, int pageSize);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<Chat> paginateByColumns(int page, int pageSize, Columns columns);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @param columns
     * @param orderBy
     * @return
     */
    public Page<Chat> paginateByColumns(int page, int pageSize, Columns columns, String orderBy);


    /**
     * 获取私人聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser);


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
    ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);


    /**
     * 获取团队聊天
     *
     * @param chatRequest 聊天请求
     * @param teamChat    团队聊天
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int teamChat, User loginUser);

    /**
     * 获得大厅聊天
     *
     * @param chatType  聊天类型
     * @param loginUser 登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getHallChat(int chatType, User loginUser);

    /**
     * 私聊
     *
     * @param request
     * @return
     */
    List<PrivateMessageVO> listPrivateChat(HttpServletRequest request);


    /**
     * 获取和好友最后一条的聊天消息
     *
     * @param userId
     * @param friendIdList
     * @return
     */
    List<Chat> getLastPrivateChatMessages(Long userId, List<Long> friendIdList);

}
