DROP TABLE IF EXISTS `group`;
CREATE TABLE `group`
(
    `id`   bigint(20)          NOT NULL COMMENT '主键id (群号)',
    `name` varchar(100)        NOT NULL COMMENT '群名',
    `type` tinyint(2) unsigned NOT NULL COMMENT '群类别 (1/音游)',
    `city` varchar(10)         NOT NULL COMMENT '城市',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='群表';
