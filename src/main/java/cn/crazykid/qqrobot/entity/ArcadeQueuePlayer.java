package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

import java.util.Date;

/**
 * ArcadeQueuePlayer: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "arcade_queue_player",
        schema = "qqrobot",
        suffix = ""
)
public class ArcadeQueuePlayer extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId(
          value = "id",
          desc = "主键id"
  )
  private Integer id;

  @TableField(
          value = "nickname",
          desc = "qq名称"
  )
  private String nickname;

  @TableField(
          value = "qq_number",
          desc = "qq号"
  )
  private Long qqNumber;

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
          value = "index",
          desc = "当前第几位"
  )
  private Integer index;

  @TableField(
          value = "status",
          desc = "状态(1/正常 2/暂离)"
  )
  private Integer status;

  @TableField(
          value = "join_queue_date",
          desc = "加入队列的时间"
  )
  private Date joinQueueDate;

  @TableField(
          value = "leave_date",
          desc = "暂离时间"
  )
  private Date leaveDate;

  @TableField(
          value = "keep_index_count",
          desc = "已维持队列位置不动的次数"
  )
  private Integer keepIndexCount;

  @TableField(
          value = "guest",
          desc = "是否是路人(0/否 1/是)"
  )
  private Integer guest;

  public Integer getId() {
    return this.id;
  }

  public ArcadeQueuePlayer setId(Integer id) {
    this.id = id;
    return this;
  }

  public String getNickname() {
    return this.nickname;
  }

  public ArcadeQueuePlayer setNickname(String nickname) {
    this.nickname = nickname;
    return this;
  }

  public Long getQqNumber() {
    return this.qqNumber;
  }

  public ArcadeQueuePlayer setQqNumber(Long qqNumber) {
    this.qqNumber = qqNumber;
    return this;
  }

  public Long getGroupNumber() {
    return this.groupNumber;
  }

  public ArcadeQueuePlayer setGroupNumber(Long groupNumber) {
    this.groupNumber = groupNumber;
    return this;
  }

  public String getArcadeName() {
    return this.arcadeName;
  }

  public ArcadeQueuePlayer setArcadeName(String arcadeName) {
    this.arcadeName = arcadeName;
    return this;
  }

  public Integer getIndex() {
    return this.index;
  }

  public ArcadeQueuePlayer setIndex(Integer index) {
    this.index = index;
    return this;
  }

  public Integer getStatus() {
    return this.status;
  }

  public ArcadeQueuePlayer setStatus(Integer status) {
    this.status = status;
    return this;
  }

  public Date getJoinQueueDate() {
    return this.joinQueueDate;
  }

  public ArcadeQueuePlayer setJoinQueueDate(Date joinQueueDate) {
    this.joinQueueDate = joinQueueDate;
    return this;
  }

  public Date getLeaveDate() {
    return this.leaveDate;
  }

  public ArcadeQueuePlayer setLeaveDate(Date leaveDate) {
    this.leaveDate = leaveDate;
    return this;
  }

  public Integer getKeepIndexCount() {
    return this.keepIndexCount;
  }

  public ArcadeQueuePlayer setKeepIndexCount(Integer keepIndexCount) {
    this.keepIndexCount = keepIndexCount;
    return this;
  }

  public Integer getGuest() {
    return this.guest;
  }

  public ArcadeQueuePlayer setGuest(Integer guest) {
    this.guest = guest;
    return this;
  }

  @Override
  public final Class entityClass() {
    return ArcadeQueuePlayer.class;
  }
}
