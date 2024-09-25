package com.xiaoxu.service.provider;


import cn.hutool.core.collection.CollUtil;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.team.TeamJoinRequest;
import com.xiaoxu.model.dto.team.TeamQueryRequest;
import com.xiaoxu.model.dto.team.TeamQuitRequest;
import com.xiaoxu.model.dto.team.TeamUpdateRequest;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.entity.UserTeam;
import com.xiaoxu.model.enums.TeamStatusEnum;
import com.xiaoxu.model.vo.TeamUserVO;
import com.xiaoxu.model.vo.UserVO;
import com.xiaoxu.service.TeamService;
import com.xiaoxu.service.UserService;
import com.xiaoxu.service.UserTeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Bean
public class TeamServiceProvider extends JbootServiceBase<Team> implements TeamService {


    private UserService userService = new UserServiceProvider();


    private UserTeamService userTeamService = new UserTeamServiceProvider();


    @Override
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？

        if (team == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NULL);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        final Long userId = loginUser.getId();
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");

        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && name.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();

        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }

        //  g. 校验用户最多创建 5 个队伍

        //校验用户最多加入 5 个队伍
        //todo 存在bug 可能同时创建100个队伍
        Columns queryWrapper = new Columns();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = DAO.findCountByColumns(queryWrapper);

        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        Object result = this.save(team);

        Long teamId = team.getId();
        if (result == null || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeam(TeamQueryRequest teamQueryRequest, boolean isAdmin) {
        Columns queryWrapper = new Columns();
        //组合查询条件
        if (teamQueryRequest != null) {
            Long id = teamQueryRequest.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQueryRequest.getIdList();
            if (CollUtil.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            //todo  实现关键词搜索
//            String searchText = teamQueryRequest.getSearchText();
//            if (StringUtils.isNotBlank(searchText)) {
//                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
//            }
            String name = teamQueryRequest.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQueryRequest.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQueryRequest.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }

            //根据创建人来查询
            Long userId = teamQueryRequest.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQueryRequest.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);

            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && TeamStatusEnum.PRIVATE.equals(statusEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // todo 不展示已过期队伍
        // expireTime is not null or expireTime  > now()
//        queryWrapper.add(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.findListByColumns(queryWrapper);
        if (CollUtil.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.findById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();

            // 赋值
            teamUserVO.setId(team.getId());
            teamUserVO.setName(team.getName());
            teamUserVO.setDescription(team.getDescription());
            teamUserVO.setMaxNum(team.getMaxNum());
            teamUserVO.setExpireTime(team.getExpireTime());
            teamUserVO.setUserId(team.getUserId());
            teamUserVO.setStatus(team.getStatus());
            teamUserVO.setCreateTime(team.getCreateTime());
            teamUserVO.setUpdateTime(team.getUpdateTime());

            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                // 赋值
                userVO.setId(user.getId());
                userVO.setUserAccount(user.getUserAccount());
                userVO.setUserName(user.getUserName());
                userVO.setUserAvatar(user.getUserAvatar());
                userVO.setUserProfile(user.getUserProfile());
                userVO.setUserRole(user.getUserRole());
                userVO.setCreateTime(user.getCreateTime());
                userVO.setUpdateTime(user.getUpdateTime());
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.findById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
        }
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 只有管理员或者队伍创建者可以修改
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码");
            }
        }
        Team updateTeam = new Team();

        updateTeam.setId(teamUpdateRequest.getId());
        updateTeam.setName(teamUpdateRequest.getName());
        updateTeam.setDescription(teamUpdateRequest.getDescription());
        updateTeam.setMaxNum(teamUpdateRequest.getMaxNum());
        updateTeam.setExpireTime(teamUpdateRequest.getExpireTime());
        updateTeam.setUserId(teamUpdateRequest.getUserId());
        updateTeam.setStatus(teamUpdateRequest.getStatus());
        updateTeam.setPassword(teamUpdateRequest.getPassword());
        return this.update(updateTeam);

    }


    @Override
    public long joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "密码错误");
            }
        }
        //该用户已加入的队伍数量
        Long userId = loginUser.getId();
        Columns userTeamQueryWrapper = new Columns();

        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.findCountByColumns(userTeamQueryWrapper);

        if (hasJoinNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");

        }
        //不能重复加入已加入的队伍
        userTeamQueryWrapper = new Columns();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasUserJoinTeam = userTeamService.findCountByColumns(userTeamQueryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");

        }
        //已加入队伍的人数
        long teamHasJoinNum = this.getTeamUserByTeamId(teamId);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍已满");
        }

        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return (long) userTeamService.save(userTeam);
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getTeamById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, " 队伍不存在");
        }
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);

//        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        Columns queryWrapper = new Columns();

        queryWrapper.eq("userId", userId);
        long count = userTeamService.findCountByColumns(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.getTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.deleteById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId().equals(userId)) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                Columns userTeamQueryWrapper = new Columns();
                userTeamQueryWrapper.eq("teamId", teamId);
                //todo  校验
                userTeamQueryWrapper.sqlPart("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.findListByColumns(userTeamQueryWrapper);
                if (CollUtil.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.update(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // todo 移除关系
        UserTeam result = userTeamService.findFirstByColumns(queryWrapper);
        return userTeamService.delete(result);
    }

    @Override
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (!loginUser.getId().equals(team.getUserId())) {

            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        Columns userTeamQueryWrapper = new Columns();

        userTeamQueryWrapper.eq("teamId", teamId);
        UserTeam firstByColumns = userTeamService.findFirstByColumns(userTeamQueryWrapper);
        boolean result = userTeamService.delete(firstByColumns);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.deleteById(teamId);
    }


    /**
     * 根据id获取队伍信息
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.findById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NULL);
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long getTeamUserByTeamId(long teamId) {
        Columns userTeamQueryWrapper = new Columns();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.findCountByColumns(userTeamQueryWrapper);
    }
}
