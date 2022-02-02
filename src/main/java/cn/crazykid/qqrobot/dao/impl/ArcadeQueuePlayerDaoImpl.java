package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.ArcadeQueuePlayerBaseDao;
import cn.crazykid.qqrobot.dao.intf.ArcadeQueuePlayerDao;
import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer;
import cn.crazykid.qqrobot.wrapper.ArcadeQueuePlayerQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ArcadeQueuePlayerDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class ArcadeQueuePlayerDaoImpl extends ArcadeQueuePlayerBaseDao implements ArcadeQueuePlayerDao {
    @Override
    public void deleteAll() {
        mapper().delete(new ArcadeQueuePlayerQuery());
    }

    @Override
    public ArcadeQueuePlayer selectOneByQQNumber(long qqNumber) {
        return mapper.findOne(new ArcadeQueuePlayerQuery()
                .where.qqNumber().eq(qqNumber).end()
                .last("limit 1"));
    }

    @Override
    public void deleteByQQNumber(long qqNumber) {
        mapper.delete(new ArcadeQueuePlayerQuery()
                .where.qqNumber().eq(qqNumber).end()
        );
    }

    @Override
    public void deleteByQQNumber(List<Long> qqNumberList) {
        mapper.delete(new ArcadeQueuePlayerQuery()
                .where.qqNumber().in(qqNumberList).end()
        );
    }

    @Override
    public List<ArcadeQueuePlayer> list(Long groupNumber, String arcadeName) {
        return listEntity(new ArcadeQueuePlayerQuery()
                .where.groupNumber().eq(groupNumber).arcadeName().eq(arcadeName).end()
        );
    }
}
