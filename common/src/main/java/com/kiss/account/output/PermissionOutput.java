package com.kiss.account.output;

import lombok.Data;

import java.util.Date;

@Data
public class PermissionOutput {
    /**
     * 权限ID
     */
    private Integer id;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 权限所属模块ID
     */
    private Integer moduleId;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 权限类型
     */
    private Integer type;
    /**
     * 权限类型文案
     */
    private String typeText;
    /**
     * 权限码
     */
    private String code;
    /**
     * 权限限制数据字段
     */
    private String limitFields;
    /**
     * 权限状态
     */
    private Integer status;

    /**
     * 权限状态文案
     */
    private String statusText;
    /**
     * 权限排序
     */
    private Integer seq;
    /**
     * 权限备注
     */
    private String remark;
    /**
     * 最后操作者ID
     */
    private Integer operatorId;
    /**
     * 最后操作者
     */
    private String operatorName;
    /**
     * 最后操作者IP
     */
    private String operatorIp;
    /**
     * 创建时间
     */
    private Date createdAt;
    /**
     * 更新时间
     */
    private Date updatedAt;
}
