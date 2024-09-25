#数据库初始化

-- 创建库
create
    database if not exists db_shixun;

-- 切换库
use db_shixun;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;


-- 队伍表
create table if not exists team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint comment '用户id（队长 id）',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '队伍' collate = utf8mb4_unicode_ci;


-- 用户队伍关系
create table if not exists user_team
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍关系' collate = utf8mb4_unicode_ci;


-- 标签表
create table if not exists tag
(
    id         bigint auto_increment comment 'id' primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户 id',
    parentId   bigint                             null comment '父标签 id',
    isParent   tinyint                            null comment '0 - 不是, 1 - 父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    constraint uniIdx_tagName
        unique (tagName)
) comment '标签' collate = utf8mb4_unicode_ci;


-- 好友表
create table friend
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             not null comment '用户id（即自己id）',
    friendId   bigint                             not null comment '好友id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '好友表';


-- 聊天表
create table chat
(
    id         bigint auto_increment comment '聊天记录id'
        primary key,
    fromId     bigint                                  not null comment '发送消息id',
    toId       bigint                                  null comment '接收消息id',
    text       varchar(512) collate utf8mb4_unicode_ci null,
    chatType   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    createTime datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP      null,
    teamId     bigint                                  null,
    isDelete   tinyint  default 0                      null
) comment '聊天消息表' collate = utf8mb4_general_ci
                       row_format = COMPACT;


-- 关注表
create table follow
(
    id         bigint auto_increment comment 'id' primary key,
    followeeId bigint                             not null comment '被关注者 id',
    followerId bigint                             not null comment '粉丝 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '关注表';


/*博客表*/
create table blog
(
    id         bigint auto_increment comment 'id'
        primary key,
    title      varchar(128)                       not null comment '标题',
    coverImage varchar(256)                       null comment '封面图片',
    images     varchar(2048)                      null comment '图片列表',
    content    text                               not null comment '内容',
    userId     bigint                             not null comment '作者 id',
    tags       varchar(256)                       null comment '标签列表',
    viewNum    bigint   default 0                 not null comment '浏览数',
    likeNum    bigint   default 0                 not null comment '点赞数',
    starNum    bigint   default 0                 not null comment '收藏数',
    commentNum bigint   default 0                 not null comment '评论数',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '博客表';

/*评论表*/
create table comment
(
    id         bigint auto_increment
        primary key,
    userId     bigint                             null comment '用户id（评论者id）',
    blogId     bigint                             null comment '博客id',
    text       varchar(512)                       null comment '评论内容',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '评论表';


/*消息表*/
create table message
(
    id         bigint auto_increment comment '主键'
        primary key,
    type       tinyint                            null comment '类型 - 0 - 收藏 1 - 点赞 2 - 关注消息 3 - 私发消息 4 - 队伍消息',
    fromId     bigint                             null comment '消息发送的用户id',
    toId       bigint                             null comment '消息接收的用户id',
    text       varchar(255)                       null comment '消息内容',
    avatarUrl  varchar(256)                       null comment '头像',
    blogId     bigint                             null comment '博客 id',
    teamId     bigint                             null comment '队伍 id',
    isRead     tinyint  default 0                 null comment '已读-0 未读 ,1 已读',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除'
)
    collate = utf8mb4_general_ci
    row_format = COMPACT;