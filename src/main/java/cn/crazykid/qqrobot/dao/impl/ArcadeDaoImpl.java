package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.ArcadeBaseDao;
import cn.crazykid.qqrobot.dao.intf.ArcadeDao;
import cn.crazykid.qqrobot.entity.Arcade;
import cn.crazykid.qqrobot.wrapper.ArcadeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ArcadeDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class ArcadeDaoImpl extends ArcadeBaseDao implements ArcadeDao {
    @Override
    public List<Arcade> selectEnableArcades() {
        return listEntity(new ArcadeQuery().where.enable().eq(1).end());
    }
}
