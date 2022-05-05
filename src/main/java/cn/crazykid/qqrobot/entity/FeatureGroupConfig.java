package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

/**
 * FeatureGroupConfig: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "feature_group_config",
        schema = "qqrobot",
        suffix = ""
)
public class FeatureGroupConfig extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId(
          value = "id",
          desc = "主键id"
  )
  private Integer id;

  @TableField(
          value = "group_id",
          desc = "群号"
  )
  private Long groupId;

  @TableField(
          value = "feature_code",
          desc = "功能代码"
  )
  private String featureCode;

  @TableField(
          value = "enable",
          desc = "是否启用(0/否 1/是)"
  )
  private Integer enable;

  @TableField(
          value = "json",
          desc = "扩展json配置"
  )
  private String json;

  public Integer getId() {
    return this.id;
  }

  public FeatureGroupConfig setId(Integer id) {
    this.id = id;
    return this;
  }

  public Long getGroupId() {
    return this.groupId;
  }

  public FeatureGroupConfig setGroupId(Long groupId) {
    this.groupId = groupId;
    return this;
  }

  public String getFeatureCode() {
    return this.featureCode;
  }

  public FeatureGroupConfig setFeatureCode(String featureCode) {
    this.featureCode = featureCode;
    return this;
  }

  public Integer getEnable() {
    return this.enable;
  }

  public FeatureGroupConfig setEnable(Integer enable) {
    this.enable = enable;
    return this;
  }

  public String getJson() {
    return this.json;
  }

  public FeatureGroupConfig setJson(String json) {
    this.json = json;
    return this;
  }

  @Override
  public final Class entityClass() {
    return FeatureGroupConfig.class;
  }
}
