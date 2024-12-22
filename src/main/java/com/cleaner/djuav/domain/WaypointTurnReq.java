package com.cleaner.djuav.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Author:songjian
 * Date: 2024/12/22 13:05
 **/
@Data
public class WaypointTurnReq implements Serializable {

    /**
     * 航点转弯模式
     */
    private String waypointTurnMode;

    /**
     * 航点转弯截距
     */
    private Double waypointTurnDampingDist;

    /**
     * 该航段是否贴合直线
     */
    private Integer useStraightLine;


}
