package com.xiaoxu.service;


import com.jfinal.plugin.activerecord.Page;
import com.xiaoxu.model.dto.team.TeamJoinRequest;
import com.xiaoxu.model.dto.team.TeamQueryRequest;
import com.xiaoxu.model.dto.team.TeamQuitRequest;
import com.xiaoxu.model.dto.team.TeamUpdateRequest;
import com.xiaoxu.model.entity.Team;
import com.xiaoxu.model.entity.User;
import com.xiaoxu.model.vo.TeamUserVO;
import io.jboot.db.model.Columns;

import java.util.List;

public interface TeamService {

    /**
     * 根据主键查找Model
     *
     * @param id
     * @return
     */
    public Team findById(Object id);


    /**
     * 根据 Columns 查找单条数据
     *
     * @param columns
     * @return
     */
    public Team findFirstByColumns(Columns columns);

    /**
     * 根据 Columns 查找单条数据
     *
     * @param columns
     * @param orderBy
     * @return
     */
    public Team findFirstByColumns(Columns columns, String orderBy);


    /**
     * 查找全部数据
     *
     * @return
     */
    public List<Team> findAll();


    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @return
     */
    public List<Team> findListByColumns(Columns columns);


    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param orderBy
     * @return
     */
    public List<Team> findListByColumns(Columns columns, String orderBy);

    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param count
     * @return
     */
    public List<Team> findListByColumns(Columns columns, Integer count);

    /**
     * 根据 Columns 查找数据
     *
     * @param columns
     * @param orderBy
     * @param count
     * @return
     */
    public List<Team> findListByColumns(Columns columns, String orderBy, Integer count);


    /**
     * 根据提交查询数据量
     *
     * @param columns
     * @return
     */
    public long findCountByColumns(Columns columns);


    /**
     * 根据ID 删除model
     *
     * @param id
     * @return
     */
    public boolean deleteById(Object id);


    /**
     * 删除
     *
     * @param model
     * @return
     */
    public boolean delete(Team model);


    /**
     * 根据 多个 id 批量删除
     *
     * @param ids
     * @return
     */
    public boolean batchDeleteByIds(Object... ids);


    /**
     * 保存到数据库
     *
     * @param model
     * @return id if success
     */
    public Object save(Team model);


    /**
     * 保存或更新
     *
     * @param model
     * @return id if success
     */
    public Object saveOrUpdate(Team model);

    /**
     * 更新
     *
     * @param model
     * @return
     */
    public boolean update(Team model);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<Team> paginate(int page, int pageSize);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<Team> paginateByColumns(int page, int pageSize, Columns columns);


    /**
     * 分页
     *
     * @param page
     * @param pageSize
     * @param columns
     * @param orderBy
     * @return
     */
    public Page<Team> paginateByColumns(int page, int pageSize, Columns columns, String orderBy);


    /**
     * 添加队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    public long addTeam(Team team, User loginUser);

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 查询队伍（脱敏）
     *
     * @param teamQueryRequest
     * @param isAdmin
     * @return
     */
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    public boolean deleteTeam(Long id, User loginUser);

}