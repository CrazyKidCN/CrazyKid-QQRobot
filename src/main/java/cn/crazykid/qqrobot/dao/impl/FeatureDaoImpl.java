package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.FeatureBaseDao;
import cn.crazykid.qqrobot.dao.intf.FeatureDao;
import cn.crazykid.qqrobot.entity.Feature;
import cn.crazykid.qqrobot.wrapper.FeatureQuery;
import org.springframework.stereotype.Repository;

/**
 * FeatureDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class FeatureDaoImpl extends FeatureBaseDao implements FeatureDao {
    @Override
    public Feature selectByCode(String code) {
        return mapper.findOne(new FeatureQuery()
                .where.code().eq(code).end());
    }
}
