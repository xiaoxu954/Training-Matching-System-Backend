package com.xiaoxu.service.provider;


import com.xiaoxu.model.dto.team.TeamJoinRequest;
import com.xiaoxu.model.dto.team.TeamQueryRequest;
import com.xiaoxu.model.dto.team.TeamQuitRequest;
import com.xiaoxu.model.dto.team.TeamUpdateRequest;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.TeamUserVO;
import com.xiaoxu.service.TeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

import java.util.Collections;
import java.util.List;

@Bean
public class TeamServiceProvider extends JbootServiceBase<Team> implements TeamService {

    @Override
    public long addTeam(Team team, User loginUser) {
        return 0;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        return false;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin) {
        return Collections.emptyList();
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        return false;
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        return false;
    }

    @Override
    public boolean deleteTeam(Long id, User loginUser) {
        return false;
    }
}
