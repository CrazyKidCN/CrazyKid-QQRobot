DROP TABLE IF EXISTS `arcade_queue_player`;
CREATE TABLE `arcade_queue_player`
(
    `id`               int(10) unsigned                NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `nickname`         varchar(100) CHARACTER SET utf8 NOT NULL COMMENT 'qq名称',
    `qq_number`        bigint(20) unsigned             NOT NULL COMMENT 'qq号',
    `group_number`     bigint(20)                      NOT NULL COMMENT '群号',
    `arcade_name`      varchar(40) CHARACTER SET utf8  NOT NULL COMMENT '机厅名',
    `index`            int(10) unsigned                NOT NULL DEFAULT '1' COMMENT '当前第几位',
    `status`           tinyint(2) unsigned             NOT NULL DEFAULT '1' COMMENT '状态(1/正常 2/暂离)',
    `join_queue_date`  datetime                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入队列的时间',
    `leave_date`       datetime                                 DEFAULT NULL COMMENT '暂离时间',
    `keep_index_count` int(10) unsigned                NOT NULL DEFAULT '0' COMMENT '已维持队列位置不动的次数',
    `guest`            tinyint(2) unsigned             NOT NULL DEFAULT '0' COMMENT '是否是路人(0/否 1/是)',
    PRIMARY KEY (`id`),
    KEY `qq_number` (`qq_number`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 139
  DEFAULT CHARSET = utf8mb4 COMMENT ='机厅排队玩家表';
