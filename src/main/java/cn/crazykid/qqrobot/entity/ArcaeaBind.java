package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

import java.util.Date;

/**
 * ArcaeaBind: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "arcaea_bind",
        schema = "qqrobot",
        suffix = ""
)
public class ArcaeaBind extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId("id")
  private Integer id;

  @TableField("arc_id")
  private Long arcId;

  @TableField("qq_number")
  private Long qqNumber;

  @TableField(
          value = "create_time",
          insert = "now()"
  )
  private Date createTime;

  @TableField(
          value = "update_time",
          insert = "now()",
          update = "now()"
  )
  private Date updateTime;

  public Integer getId() {
    return this.id;
  }

  public ArcaeaBind setId(Integer id) {
    this.id = id;
    return this;
  }

  public Long getArcId() {
    return this.arcId;
  }

  public ArcaeaBind setArcId(Long arcId) {
    this.arcId = arcId;
    return this;
  }

  public Long getQqNumber() {
    return this.qqNumber;
  }

  public ArcaeaBind setQqNumber(Long qqNumber) {
    this.qqNumber = qqNumber;
    return this;
  }

  public Date getCreateTime() {
    return this.createTime;
  }

  public ArcaeaBind setCreateTime(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public Date getUpdateTime() {
    return this.updateTime;
  }

  public ArcaeaBind setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  @Override
  public final Class entityClass() {
    return ArcaeaBind.class;
  }
}
