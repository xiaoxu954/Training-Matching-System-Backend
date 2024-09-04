package com.xiaoxu.provider;


import com.xiaoxu.model.entity.Team;
import com.xiaoxu.service.TeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class TeamServiceProvider extends JbootServiceBase<Team> implements TeamService {

}
