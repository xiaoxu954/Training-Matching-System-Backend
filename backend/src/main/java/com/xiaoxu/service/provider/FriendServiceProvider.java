package com.xiaoxu.service.provider;


import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.friend.FriendQueryRequest;
import com.xiaoxu.model.entity.Friend;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.FriendService;
import com.xiaoxu.service.UserService;
import com.xiaoxu.service.UserTeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Bean
public class FriendServiceProvider extends JbootServiceBase<Friend> implements FriendService {


    private UserService userService = new UserServiceProvider();


    private UserTeamService userTeamService = new UserTeamServiceProvider();


    @Override
    public boolean addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "自己不能添加自己为好友'");
        }
        // 设置锁名称，锁范围是同一个人加同一个人为好友
        // 设置锁名称，锁范围是同一个人加同一个人为好友
        boolean result1 = false;
        boolean result2 = false;


        try {
            // 查询是否添加了该用户
            Columns queryWrapper = new Columns();
            queryWrapper.eq("userId", userId);
            queryWrapper.eq("friendId", friendId);
            long count1 = this.findCountByColumns(queryWrapper);

            if (count1 > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "已添加该用户");
            }
            // 查询是否添加了该用户
            queryWrapper = new Columns();

            queryWrapper.eq("userId", friendId);
            queryWrapper.eq("friendId", userId);
            Long count2 = this.findCountByColumns(queryWrapper);
            if (count2 > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "已添加该用户");
            }
            // 插入id: userId, friendId: friendId
            Friend friendByUserId = new Friend();
            friendByUserId.setUserId(userId);
            friendByUserId.setFriendId(friendId);
            // 插入id:friendId , friendId: userId（注意添加事务，即要么都添加要么都不添加）
            Object save = this.save(friendByUserId);
            result1 = (boolean) save;
            Friend friendByFriendId = new Friend();
            friendByFriendId.setUserId(friendId);
            friendByFriendId.setFriendId(userId);
            // 写入数据库
            Object save2 = this.save(friendByFriendId);
            result2 = (boolean) save2;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("addUser error", e);
        }

        return result1 && result2;
    }

    @Override
    public List<UserVO> listFriends(Long userId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Columns queryWrapper = new Columns();
        queryWrapper.eq("userId", userId);
        // 查询登录用户的所有好友
        List<Friend> friendList = this.findListByColumns(queryWrapper);

        List<User> userList = friendList.stream().map(friend -> {
            User user = userService.findById(friend.getFriendId());
            return user;
        }).collect(Collectors.toList());


        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUserName(user.getUserName());
            userVO.setUserAccount(user.getUserAccount());
            userVO.setUserAvatar(user.getUserAvatar());
            userVO.setUserProfile(user.getUserProfile());
            userVO.setCreateTime(user.getCreateTime());
            userVO.setUpdateTime(user.getUpdateTime());
            userVO.setUserRole(user.getUserRole());
            userVO.setTags(user.getTags());
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }

    @Override
    public List<UserVO> searchFriends(FriendQueryRequest friendQueryRequest, long userId) {
        String searchParam = friendQueryRequest.getSearchParam();
        Columns queryWrapper = new Columns();
        queryWrapper.eq("userId", userId);
        // 查询登录用户的所有好友
        List<Friend> friendList = this.findListByColumns(queryWrapper);
        List<Long> frinedIdList = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
        // 根据 id 和 username 查询目标用户
        Columns userQueryWrapper = new Columns();
        userQueryWrapper.in("id", frinedIdList);
        userQueryWrapper.like("username", searchParam);
        List<User> userList = userService.findListByColumns(userQueryWrapper);

        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUserName(user.getUserName());
            userVO.setUserAccount(user.getUserAccount());
            userVO.setUserAvatar(user.getUserAvatar());
            userVO.setUserProfile(user.getUserProfile());
            userVO.setCreateTime(user.getCreateTime());
            userVO.setUpdateTime(user.getUpdateTime());
            userVO.setUserRole(user.getUserRole());
            userVO.setTags(user.getTags());
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }
}
