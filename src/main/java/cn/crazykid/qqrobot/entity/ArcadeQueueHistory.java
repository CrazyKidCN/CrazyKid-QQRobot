package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

import java.util.Date;

/**
 * ArcadeQueueHistory: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "arcade_queue_history",
        schema = "qqrobot",
        suffix = ""
)
public class ArcadeQueueHistory extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId(
          value = "id",
          desc = "主键id"
  )
  private Integer id;

  @TableField(
          value = "group_number",
          desc = "群号"
  )
  private Long groupNumber;

  @TableField(
          value = "arcade_name",
          desc = "机厅名"
  )
  private String arcadeName;

  @TableField(
          value = "create_by",
          desc = "创建人名称"
  )
  private String createBy;

  @TableField(
          value = "create_qq_number",
          desc = "创建人QQ号"
  )
  private Long createQqNumber;

  @TableField(
          value = "create_time",
          insert = "now()",
          desc = "创建时间"
  )
  private Date createTime;

  @TableField(
          value = "queue_json",
          desc = "队列json"
  )
  private String queueJson;

  public Integer getId() {
    return this.id;
  }

  public ArcadeQueueHistory setId(Integer id) {
    this.id = id;
    return this;
  }

  public Long getGroupNumber() {
    return this.groupNumber;
  }

  public ArcadeQueueHistory setGroupNumber(Long groupNumber) {
    this.groupNumber = groupNumber;
    return this;
  }

  public String getArcadeName() {
    return this.arcadeName;
  }

  public ArcadeQueueHistory setArcadeName(String arcadeName) {
    this.arcadeName = arcadeName;
    return this;
  }

  public String getCreateBy() {
    return this.createBy;
  }

  public ArcadeQueueHistory setCreateBy(String createBy) {
    this.createBy = createBy;
    return this;
  }

  public Long getCreateQqNumber() {
    return this.createQqNumber;
  }

  public ArcadeQueueHistory setCreateQqNumber(Long createQqNumber) {
    this.createQqNumber = createQqNumber;
    return this;
  }

  public Date getCreateTime() {
    return this.createTime;
  }

  public ArcadeQueueHistory setCreateTime(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public String getQueueJson() {
    return this.queueJson;
  }

  public ArcadeQueueHistory setQueueJson(String queueJson) {
    this.queueJson = queueJson;
    return this;
  }

  @Override
  public final Class entityClass() {
    return ArcadeQueueHistory.class;
  }
}
