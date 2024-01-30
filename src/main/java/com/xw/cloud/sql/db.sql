drop table T_Cloud_Constructions;
CREATE TABLE T_Cloud_Constructions
(
    ID INTEGER NOT NULL AUTO_INCREMENT,
    TaskName varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
    VmName varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  COMMENT '虚拟机名称',
    ImgName varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  COMMENT '代码包名称',
    OSType varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  COMMENT '系统类型',
    NetType varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  COMMENT '网络类型',
    CPUNum INTEGER CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'CPU数',
    Memory INTEGER CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '内存',
    Port INTEGER CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '端口',
    Disk INTEGER CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '磁盘大小',
    ServerIp varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '宿主机IP',
    Cmd varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci  COMMENT '执行命令',
    OperationOrder INTEGER NOT NULL COMMENT '执行顺序',
    OperationStatus INTEGER  COMMENT '操作是否正常，1正常操作， 0 操作异常',
    primary key(id)
);