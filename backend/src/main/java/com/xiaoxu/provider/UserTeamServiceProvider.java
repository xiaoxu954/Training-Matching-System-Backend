package com.xiaoxu.provider;


import com.xiaoxu.model.entity.UserTeam;
import com.xiaoxu.service.UserTeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class UserTeamServiceProvider extends JbootServiceBase<UserTeam> implements UserTeamService {

}
