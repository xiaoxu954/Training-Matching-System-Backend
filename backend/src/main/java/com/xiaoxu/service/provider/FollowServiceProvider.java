package com.xiaoxu.service.provider;


import com.xiaoxu.model.entity.Follow;
import com.xiaoxu.service.FollowService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class FollowServiceProvider extends JbootServiceBase<Follow> implements FollowService {

}
