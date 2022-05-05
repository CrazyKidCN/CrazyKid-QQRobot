package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

/**
 * Feature: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "feature",
        schema = "qqrobot",
        suffix = ""
)
public class Feature extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId(
          value = "id",
          desc = "主键id"
  )
  private Integer id;

  @TableField(
          value = "code",
          desc = "功能代码"
  )
  private String code;

  @TableField(
          value = "title",
          desc = "功能标题"
  )
  private String title;

  @TableField(
          value = "desc",
          desc = "功能描述"
  )
  private String desc;

  @TableField(
          value = "default_enable",
          desc = "默认是否启用 (0/否 1/是)"
  )
  private Integer defaultEnable;

  @TableField(
          value = "show_flag",
          desc = "是否展示(0/否 1/是)"
  )
  private Integer showFlag;

  public Integer getId() {
    return this.id;
  }

  public Feature setId(Integer id) {
    this.id = id;
    return this;
  }

  public String getCode() {
    return this.code;
  }

  public Feature setCode(String code) {
    this.code = code;
    return this;
  }

  public String getTitle() {
    return this.title;
  }

  public Feature setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getDesc() {
    return this.desc;
  }

  public Feature setDesc(String desc) {
    this.desc = desc;
    return this;
  }

  public Integer getDefaultEnable() {
    return this.defaultEnable;
  }

  public Feature setDefaultEnable(Integer defaultEnable) {
    this.defaultEnable = defaultEnable;
    return this;
  }

  public Integer getShowFlag() {
    return this.showFlag;
  }

  public Feature setShowFlag(Integer showFlag) {
    this.showFlag = showFlag;
    return this;
  }

  @Override
  public final Class entityClass() {
    return Feature.class;
  }
}
