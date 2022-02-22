DROP TABLE IF EXISTS `arcade`;
CREATE TABLE `arcade`
(
    `id`                int(10) unsigned                NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`              varchar(30) CHARACTER SET utf8  NOT NULL COMMENT '机厅名称',
    `alias_json`        varchar(255) CHARACTER SET utf8 NOT NULL DEFAULT '[]' COMMENT '别名',
    `city`              varchar(10) CHARACTER SET utf8  NOT NULL COMMENT '城市',
    `address`           varchar(255) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '机厅地址',
    `machine_num`       int(10) unsigned                NOT NULL DEFAULT '1' COMMENT '机台数',
    `group_number_json` varchar(255) CHARACTER SET utf8 NOT NULL DEFAULT '[]' COMMENT '可用QQ群 (json数组)',
    `card_num`          int(10) unsigned                NOT NULL DEFAULT '0' COMMENT '当前卡数',
    `card_update_by`    varchar(100) CHARACTER SET utf8          DEFAULT NULL COMMENT '卡数更新人',
    `card_update_time`  datetime                                 DEFAULT NULL COMMENT '卡数更新时间',
    `create_by`         varchar(100) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`       datetime                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(100) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '更新人',
    `update_time`       datetime                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `enable`            tinyint(1) unsigned             NOT NULL DEFAULT '1' COMMENT '是否启用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 50
  DEFAULT CHARSET = utf8mb4 COMMENT ='机厅表';
