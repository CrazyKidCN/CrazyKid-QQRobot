package cn.crazykid.qqrobot.dao.intf;

import cn.crazykid.qqrobot.entity.ArcadeQueuePlayer;
import cn.org.atool.fluent.mybatis.base.IBaseDao;

import java.util.List;

/**
 * ArcadeQueuePlayerDao: 数据操作接口
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
public interface ArcadeQueuePlayerDao extends IBaseDao<ArcadeQueuePlayer> {
    ArcadeQueuePlayer selectOneByQQNumber(long qqNumber);

    void deleteByQQNumber(long qqNumber);

    void deleteByQQNumber(List<Long> qqNumberList);

    List<ArcadeQueuePlayer> list(Long groupNumber, String arcadeName);
}
