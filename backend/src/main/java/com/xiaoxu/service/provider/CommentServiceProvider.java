package com.xiaoxu.service.provider;


import com.xiaoxu.model.entity.Comment;
import com.xiaoxu.service.CommentService;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;

@Bean
public class CommentServiceProvider extends JbootServiceBase<Comment> implements CommentService {

}
