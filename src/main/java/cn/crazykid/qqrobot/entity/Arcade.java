package cn.crazykid.qqrobot.entity;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.annotation.TableField;
import cn.org.atool.fluent.mybatis.annotation.TableId;
import cn.org.atool.fluent.mybatis.base.RichEntity;

import java.util.Date;

/**
 * Arcade: 数据映射实体定义
 *
 * @author Powered By Fluent Mybatis
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@FluentMybatis(
        table = "arcade",
        schema = "qqrobot",
        suffix = ""
)
public class Arcade extends RichEntity {
  private static final long serialVersionUID = 1L;

  @TableId(
          value = "id",
          desc = "主键id"
  )
  private Integer id;

  @TableField(
          value = "name",
          desc = "机厅名称"
  )
  private String name;

  @TableField(
          value = "alias_json",
          desc = "别名"
  )
  private String aliasJson;

  @TableField(
          value = "city",
          desc = "城市"
  )
  private String city;

  @TableField(
          value = "address",
          desc = "机厅地址"
  )
  private String address;

  @TableField(
          value = "machine_num",
          desc = "机台数"
  )
  private Integer machineNum;

  @TableField(
          value = "group_number_json",
          desc = "可用QQ群 (json数组)"
  )
  private String groupNumberJson;

  @TableField(
          value = "card_num",
          desc = "当前卡数"
  )
  private Integer cardNum;

  @TableField(
          value = "card_update_by",
          desc = "卡数更新人"
  )
  private String cardUpdateBy;

  @TableField(
          value = "card_update_time",
          desc = "卡数更新时间"
  )
  private Date cardUpdateTime;

  @TableField(
          value = "create_by",
          desc = "创建人"
  )
  private String createBy;

  @TableField(
          value = "create_time",
          insert = "now()",
          desc = "创建时间"
  )
  private Date createTime;

  @TableField(
          value = "update_by",
          desc = "更新人"
  )
  private String updateBy;

  @TableField(
          value = "update_time",
          insert = "now()",
          update = "now()",
          desc = "更新时间"
  )
  private Date updateTime;

  @TableField(
          value = "enable",
          desc = "是否启用"
  )
  private Integer enable;

  @TableField(
          value = "close",
          desc = "是否闭店中(0/否 1/是)"
  )
  private Integer close;

  public Integer getId() {
    return this.id;
  }

  public Arcade setId(Integer id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return this.name;
  }

  public Arcade setName(String name) {
    this.name = name;
    return this;
  }

  public String getAliasJson() {
    return this.aliasJson;
  }

  public Arcade setAliasJson(String aliasJson) {
    this.aliasJson = aliasJson;
    return this;
  }

  public String getCity() {
    return this.city;
  }

  public Arcade setCity(String city) {
    this.city = city;
    return this;
  }

  public String getAddress() {
    return this.address;
  }

  public Arcade setAddress(String address) {
    this.address = address;
    return this;
  }

  public Integer getMachineNum() {
    return this.machineNum;
  }

  public Arcade setMachineNum(Integer machineNum) {
    this.machineNum = machineNum;
    return this;
  }

  public String getGroupNumberJson() {
    return this.groupNumberJson;
  }

  public Arcade setGroupNumberJson(String groupNumberJson) {
    this.groupNumberJson = groupNumberJson;
    return this;
  }

  public Integer getCardNum() {
    return this.cardNum;
  }

  public Arcade setCardNum(Integer cardNum) {
    this.cardNum = cardNum;
    return this;
  }

  public String getCardUpdateBy() {
    return this.cardUpdateBy;
  }

  public Arcade setCardUpdateBy(String cardUpdateBy) {
    this.cardUpdateBy = cardUpdateBy;
    return this;
  }

  public Date getCardUpdateTime() {
    return this.cardUpdateTime;
  }

  public Arcade setCardUpdateTime(Date cardUpdateTime) {
    this.cardUpdateTime = cardUpdateTime;
    return this;
  }

  public String getCreateBy() {
    return this.createBy;
  }

  public Arcade setCreateBy(String createBy) {
    this.createBy = createBy;
    return this;
  }

  public Date getCreateTime() {
    return this.createTime;
  }

  public Arcade setCreateTime(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public String getUpdateBy() {
    return this.updateBy;
  }

  public Arcade setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
    return this;
  }

  public Date getUpdateTime() {
    return this.updateTime;
  }

  public Arcade setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  public Integer getEnable() {
    return this.enable;
  }

  public Arcade setEnable(Integer enable) {
    this.enable = enable;
    return this;
  }

  @Override
  public final Class entityClass() {
    return Arcade.class;
  }

  public Integer getClose() {
    return close;
  }

  public void setClose(Integer close) {
    this.close = close;
  }
}
