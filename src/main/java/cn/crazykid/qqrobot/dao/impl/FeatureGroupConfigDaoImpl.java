package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.FeatureGroupConfigBaseDao;
import cn.crazykid.qqrobot.dao.intf.FeatureGroupConfigDao;
import cn.crazykid.qqrobot.entity.FeatureGroupConfig;
import cn.crazykid.qqrobot.wrapper.FeatureGroupConfigQuery;
import org.springframework.stereotype.Repository;

/**
 * FeatureGroupConfigDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class FeatureGroupConfigDaoImpl extends FeatureGroupConfigBaseDao implements FeatureGroupConfigDao {
    @Override
    public FeatureGroupConfig selectByGroupIdAndFeatureCode(Long groupId, String featureCode) {
        return mapper.findOne(new FeatureGroupConfigQuery()
                .where.groupId().eq(groupId).featureCode().eq(featureCode).end());
    }
}
