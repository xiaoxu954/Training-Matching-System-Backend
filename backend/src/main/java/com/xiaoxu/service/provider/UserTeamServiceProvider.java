package com.xiaoxu.service.provider;


import com.xiaoxu.model.entity.UserTeam;
import com.xiaoxu.service.UserTeamService;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;

@Bean
public class UserTeamServiceProvider extends JbootServiceBase<UserTeam> implements UserTeamService {


    @Override
    public boolean teamHasUser(long teamId, long userId) {
        Columns columns = new Columns();
        Columns eq = columns.eq("teamId", teamId).eq("userId", userId);
        long countByColumns = this.findCountByColumns(eq);
        return countByColumns > 0;
    }
}
