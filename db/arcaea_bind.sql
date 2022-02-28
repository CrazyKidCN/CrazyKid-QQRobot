DROP TABLE IF EXISTS `arcaea_bind`;
CREATE TABLE `arcaea_bind`
(
    `id`          int(10) unsigned    NOT NULL AUTO_INCREMENT,
    `arc_id`      bigint(20) unsigned NOT NULL,
    `qq_number`   bigint(20) unsigned NOT NULL,
    `create_time` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `arc_id` (`arc_id`, `qq_number`) USING BTREE,
    KEY `qq_number` (`qq_number`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 30
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC COMMENT ='QQ与ArcaeaId绑定表';
