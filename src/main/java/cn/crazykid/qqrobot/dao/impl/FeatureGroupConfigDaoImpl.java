package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.FeatureGroupConfigBaseDao;
import cn.crazykid.qqrobot.dao.intf.FeatureGroupConfigDao;
import cn.crazykid.qqrobot.entity.FeatureGroupConfig;
import cn.crazykid.qqrobot.entity.dto.GroupFeatureConfigDTO;
import cn.crazykid.qqrobot.wrapper.FeatureGroupConfigQuery;
import cn.crazykid.qqrobot.wrapper.FeatureQuery;
import cn.org.atool.fluent.mybatis.base.crud.JoinBuilder;
import cn.org.atool.fluent.mybatis.segment.JoinQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<GroupFeatureConfigDTO> selectGroupFeatureConfigList(Long groupId) {

        FeatureQuery featureQuery = new FeatureQuery("f")
                .select.id().title().desc().defaultEnable().end()
                .where.showFlag().eq(1).end();

        FeatureGroupConfigQuery featureGroupConfigQuery = new FeatureGroupConfigQuery("fgc")
                .select.apply("ifnull(fgc.enable, f.default_enable) as enable").end();

        JoinQuery query = JoinBuilder.from(featureQuery)
                .leftJoin(featureGroupConfigQuery)
                .on(l -> l.where.code(), r -> r.where.featureCode())
                .onApply("fgc.group_id = ?", groupId)
                .endJoin()
                .build();
        return listPoJos(GroupFeatureConfigDTO.class, query);
    }
}
