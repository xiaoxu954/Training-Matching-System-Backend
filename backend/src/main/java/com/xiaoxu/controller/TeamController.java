package com.xiaoxu.controller;


import cn.hutool.core.bean.BeanUtil;
import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.commom.BaseResponse;
import com.xiaoxu.commom.DeleteRequest;
import com.xiaoxu.commom.ErrorCode;
import com.xiaoxu.commom.ResultUtils;
import com.xiaoxu.exception.BusinessException;
import com.xiaoxu.model.dto.team.*;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.entity.UserTeam;
import com.xiaoxu.model.vo.TeamUserVO;
import com.xiaoxu.service.TeamService;
import com.xiaoxu.service.UserService;
import com.xiaoxu.service.UserTeamService;
import io.jboot.db.model.Columns;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EnableCORS
@RequestMapping("/api/team")
public class TeamController extends JbootController {

    @Inject
    private TeamService teamService;

    @Inject
    private UserService userService;

    @Inject
    private UserTeamService userTeamService;


    public void addTeam(@JsonBody TeamAddRequest teamAddRequest) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        Team team = new Team();
        team.setName(teamAddRequest.getName());
        team.setDescription(teamAddRequest.getDescription());
        team.setMaxNum(teamAddRequest.getMaxNum());
        team.setPassword(teamAddRequest.getPassword());
        team.setExpireTime(teamAddRequest.getExpireTime());

        team.setUserId(teamAddRequest.getUserId());
        team.setStatus(teamAddRequest.getStatus());

        long teamId = teamService.addTeam(team, loginUser);
        renderJson(Ret.ok("data", teamId));
    }


    public void updateTeam(@JsonBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        renderJson(Ret.of("data", true));

    }

    public void getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.findById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NULL);
        }
        renderJson(Ret.of("data", team));
    }


    public void listTeams(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpServletRequest request = getRequest();

        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest, isAdmin);
        // 2、判断当前用户是否已加入队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        Columns userTeamJoinQueryWrapper = new Columns();

        try {
            User loginUser = userService.getLoginUser(request);
            userTeamJoinQueryWrapper.eq("userId", userService.getLoginUser(request).getId());
            userTeamJoinQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.findListByColumns(userTeamJoinQueryWrapper);

            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
//        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.findListByColumns(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        renderJson(Ret.ok("data", teamList));
    }

    //查询分页
    public BaseResponse<Page<Team>> listPageTeams(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();

        //todo 检验是否有用
        BeanUtil.copyProperties(teamQueryRequest, team);


//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Columns teamQueryWrapper = new Columns();

        Page<Team> resultPage = teamService.paginateByColumns(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize(), teamQueryWrapper);
        return ResultUtils.success(resultPage);
    }


    public BaseResponse<Boolean> joinTeam(@JsonBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);

    }


    public BaseResponse<Boolean> quitTeam(@JsonBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, userLogin);
        return ResultUtils.success(result);
    }

    public BaseResponse<Boolean> deleteTeam(@JsonBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(getRequest());
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQueryRequest
     * @return
     */
    public void listMyCreateTeams(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpServletRequest request = getRequest();
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest, true);


        // 2、判断当前用户是否已加入队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 3、查询已加入队伍的人数
//        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        Columns userTeamJoinQueryWrapper = new Columns();

        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.findListByColumns(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        renderJson(Ret.ok("data", teamList));
    }

    /**
     * 获取我加入的队伍
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    public void listMyJoinTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
//        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        Columns queryWrapper = new Columns();

        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.findListByColumns(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQueryRequest.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQueryRequest, true);
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 3、查询已加入队伍的人数
//        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        Columns userTeamJoinQueryWrapper = new Columns();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        renderJson(Ret.ok("data", teamList));
    }

}
