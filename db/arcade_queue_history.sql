DROP TABLE IF EXISTS `arcade_queue_history`;
CREATE TABLE `arcade_queue_history`
(
    `id`               int(10) unsigned                NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `group_number`     bigint(20) unsigned             NOT NULL COMMENT '群号',
    `arcade_name`      varchar(40) CHARACTER SET utf8  NOT NULL COMMENT '机厅名',
    `queue_json`       text CHARACTER SET utf8         NOT NULL COMMENT '队列json',
    `create_by`        varchar(100) CHARACTER SET utf8 NOT NULL COMMENT '创建人名称',
    `create_qq_number` bigint(20) unsigned             NOT NULL COMMENT '创建人QQ号',
    `create_time`      datetime                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `group_number` (`group_number`, `arcade_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 232
  DEFAULT CHARSET = utf8mb4 COMMENT ='机厅排队操作记录';
