package com.xiaoxu.service.provider;


import com.xiaoxu.model.entity.Blog;
import com.xiaoxu.service.BlogService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class BlogServiceProvider extends JbootServiceBase<Blog> implements BlogService {

}
