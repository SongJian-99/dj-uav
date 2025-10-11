package com.cleaner.djuav.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Author:Cleaner
 * Date: 2024/12/22 10:46
 **/
@Data
public class ActionGroupReq implements Serializable {

    /**
     * 动作组编号
     */
    private Integer actionGroupId;

    /**
     * 动作组开始生效的航点
     */
    private Integer actionGroupStartIndex;

    /**
     * 动作组结束生效的航点
     */
    private Integer actionGroupEndIndex;

    /**
     * 动作触发器类型
     */
    private String actionTriggerType;

    /**
     * 动作触发器参数
     */
    private Double actionTriggerParam;

    /**
     * 动作列表
     */
    private List<PointActionReq> actions;
}
