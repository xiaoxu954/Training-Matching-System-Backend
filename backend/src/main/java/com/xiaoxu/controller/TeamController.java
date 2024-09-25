package com.xiaoxu.controller;


import com.jfinal.aop.Inject;
import com.jfinal.ext.cors.EnableCORS;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.commom.DeleteRequest;
import com.xiaoxu.commom.ErrorCode;
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
import io.jboot.support.swagger.ParamType;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.GetRequest;
import io.jboot.web.controller.annotation.PostRequest;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.json.JsonBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EnableCORS(allowOrigin = "http://localhost:3002")
@RequestMapping("/api/team")
@Api(description = "队伍API", tags = "队伍接口")
public class TeamController extends JbootController {

    @Inject
    private TeamService teamService;

    @Inject
    private UserService userService;

    @Inject
    private UserTeamService userTeamService;


    /**
     * 添加队伍
     *
     * @param teamAddRequest
     */
    @ApiOperation(value = "添加队伍", httpMethod = "Post", notes = "添加队伍")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "name", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "description", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "maxNum", paramType = ParamType.FORM, dataType = "int", required = true),
            @ApiImplicitParam(value = "password", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "expireTime", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "userId", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "status", paramType = ParamType.FORM, dataType = "string", required = true)
    })
    @PostRequest
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


    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     */
    @ApiOperation(value = "更新队伍", httpMethod = "Post", notes = "更新队伍")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "id", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "name", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "description", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "maxNum", paramType = ParamType.FORM, dataType = "int", required = true),
            @ApiImplicitParam(value = "expireTime", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "userId", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "status", paramType = ParamType.FORM, dataType = "string", required = true),
            @ApiImplicitParam(value = "password", paramType = ParamType.FORM, dataType = "string", required = true),
    })
    @PostRequest
    public void updateTeam(@JsonBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        renderJson(Ret.ok("data", true));

    }

    @ApiOperation(value = "根据id获取队伍", httpMethod = "Post", notes = "根据id获取队伍")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "id", paramType = ParamType.FORM, dataType = "string", required = true),
    })
    @GetRequest
    public void getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.findById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NULL);
        }
        renderJson(Ret.ok("data", team));
    }

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     */
    @ApiImplicitParams({
            @ApiImplicitParam(value = "id", paramType = ParamType.FORM, dataType = "string", required = true),
    })
    @ApiOperation(value = "查询队伍列表", httpMethod = "Get", notes = "查询队伍列表")
    public void listTeam(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String status = getPara("status");
        String searchText = getPara("searchText");
        String pageNum = getPara("pageNum");
        teamQueryRequest.setStatus(Integer.valueOf(status));
        teamQueryRequest.setCurrent(Integer.parseInt(pageNum));
        teamQueryRequest.setSearchText(searchText);
        HttpServletRequest request = getRequest();

        boolean isAdmin = userService.isAdmin(getRequest());
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeam(teamQueryRequest, isAdmin);
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
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.findListByColumns(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        renderJson(Ret.ok("data", teamList));
    }

    /**
     * 分页查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @ApiOperation(value = "分页查询队伍列表", httpMethod = "Post", notes = "分页查询队伍列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "teamQueryRequest", value = "队伍查询请求", required = true, dataType = "TeamQueryRequest")
    })
    public void listTeamByPage(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Columns teamQueryWrapper = new Columns();

        teamQueryWrapper.eq("name", teamQueryRequest.getName());
        teamQueryWrapper.eq("description", teamQueryRequest.getDescription());
        teamQueryWrapper.eq("maxNum", teamQueryRequest.getMaxNum());
        teamQueryWrapper.eq("status", teamQueryRequest.getStatus());

        Page<Team> resultPage = teamService.paginateByColumns(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize(), teamQueryWrapper);
        renderJson(Ret.ok("data", resultPage));
    }


    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     */
    @PostRequest
    public void joinTeam(@JsonBody TeamJoinRequest teamJoinRequest) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        long result = teamService.joinTeam(teamJoinRequest, loginUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败");
        }
        renderJson(Ret.ok("data", result));

    }


    /**
     * 用户退出队伍
     *
     * @param teamQuitRequest
     */
    public void quitTeam(@JsonBody TeamQuitRequest teamQuitRequest) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getLoginUser(getRequest());
        boolean result = teamService.quitTeam(teamQuitRequest, userLogin);
        renderJson(Ret.ok("data", result));
    }


    /**
     * 删除队伍
     *
     * @param deleteRequest
     * @return
     */
    public void deleteTeam(@JsonBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(getRequest());
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        renderJson(Ret.ok("data", result));
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQueryRequest
     * @return
     */
    @GetRequest
    public void listMyCreateTeam(TeamQueryRequest teamQueryRequest) {

        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HttpServletRequest request = getRequest();
        User loginUser = userService.getLoginUser(request);
        teamQueryRequest.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeam(teamQueryRequest, true);

        // 2、判断当前用户是否已加入队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 3、查询已加入队伍的人数
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
     * @return
     */
    //todo 修复bug 若未加入任何队伍 将查出所有公开队伍返回
    public void listMyJoinTeam(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(getRequest());
        Columns queryWrapper = new Columns();

        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.findListByColumns(queryWrapper);

        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQueryRequest.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeam(teamQueryRequest, true);
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 3、查询已加入队伍的人数
        Columns userTeamJoinQueryWrapper = new Columns();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        renderJson(Ret.ok("data", teamList));
    }

}
