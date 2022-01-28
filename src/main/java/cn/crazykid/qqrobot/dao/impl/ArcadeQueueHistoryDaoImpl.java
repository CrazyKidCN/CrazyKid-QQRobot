package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.ArcadeQueueHistoryBaseDao;
import cn.crazykid.qqrobot.dao.intf.ArcadeQueueHistoryDao;
import cn.crazykid.qqrobot.entity.ArcadeQueueHistory;
import cn.crazykid.qqrobot.wrapper.ArcadeQueueHistoryQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ArcadeQueueHistoryDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class ArcadeQueueHistoryDaoImpl extends ArcadeQueueHistoryBaseDao implements ArcadeQueueHistoryDao {
    @Override
    public void deleteAll() {
        mapper().delete(new ArcadeQueueHistoryQuery());
    }

    @Override
    public List<ArcadeQueueHistory> selectHistory(long groupNumber, String arcadeName, int limit) {
        return listEntity(new ArcadeQueueHistoryQuery()
                .where.groupNumber().eq(groupNumber).arcadeName().eq(arcadeName).end()
                .orderBy.createTime().desc().end()
                .last("limit " + limit));
    }
}
