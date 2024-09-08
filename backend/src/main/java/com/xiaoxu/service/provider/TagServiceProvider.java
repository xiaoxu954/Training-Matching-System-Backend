package com.xiaoxu.service.provider;


import com.xiaoxu.model.entity.Tag;
import com.xiaoxu.service.TagService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class TagServiceProvider extends JbootServiceBase<Tag> implements TagService {

}
