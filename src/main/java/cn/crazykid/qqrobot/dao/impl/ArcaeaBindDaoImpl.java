package cn.crazykid.qqrobot.dao.impl;

import cn.crazykid.qqrobot.dao.base.ArcaeaBindBaseDao;
import cn.crazykid.qqrobot.dao.intf.ArcaeaBindDao;
import cn.crazykid.qqrobot.entity.ArcaeaBind;
import cn.crazykid.qqrobot.wrapper.ArcaeaBindQuery;
import org.springframework.stereotype.Repository;

/**
 * ArcaeaBindDaoImpl: 数据操作接口实现
 * <p>
 * 这只是一个减少手工创建的模板文件
 * 可以任意添加方法和实现, 更改作者和重定义类名
 * <p/>@author Powered By Fluent Mybatis
 */
@Repository
public class ArcaeaBindDaoImpl extends ArcaeaBindBaseDao implements ArcaeaBindDao {
    @Override
    public ArcaeaBind getByQQ(Long qqNumber) {
        return mapper.findOne(new ArcaeaBindQuery()
                .where.qqNumber().eq(qqNumber).end());
    }

    @Override
    public void bindArcaeaId(long qqNumber, long arcaeaId) {
        ArcaeaBind arcBind = this.getByQQ(qqNumber);
        if (arcBind == null) {
            arcBind = new ArcaeaBind() {{
                setQqNumber(qqNumber);
                setArcId(arcaeaId);
            }};
            this.save(arcBind);
        } else {
            arcBind.setArcId(arcaeaId);
            this.updateById(arcBind);
        }
    }
}
