package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

/**
 * Group: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "group",
        schema = "qqrobot",
        suffix = ""
)
public class Group extends RichEntity {
    private static final long serialVersionUID = 1L;

    @TableId(
            value = "id",
            auto = false,
            desc = "主键id (群号)"
    )
    private Long id;

    @TableField(
            value = "name",
            desc = "群名"
    )
    private String name;

    @TableField(
            value = "type",
            desc = "群类别 (1/音游)"
    )
    private Integer type;

    @TableField(
            value = "city",
            desc = "城市"
    )
    private String city;

    public Long getId() {
        return this.id;
    }

    public Group setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getType() {
        return this.type;
    }

    public Group setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getCity() {
        return this.city;
    }

    public Group setCity(String city) {
        this.city = city;
        return this;
    }

    @Override
    public final Class entityClass() {
        return Group.class;
    }
}
