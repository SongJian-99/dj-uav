package com.cleaner.djuav.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoutePointReq implements Serializable {

    /**
     * 航点编号
     */
    private Integer routePointIndex;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 高度
     */
    private Double height;

    /**
     * 飞行速度
     */
    private Double speed;

    /**
     * 航点偏航角
     */
    private WaypointHeadingReq waypointHeadingReq;

    /**
     * 航点转弯模式
     */
    private WaypointTurnReq waypointTurnReq;

    /**
     * 航点云台俯仰角
     */
    private Double gimbalPitchAngle;

    /**
     * 航点动作列表
     */
    private List<PointActionReq> actions;


    /**
     * 航点动作触发
     */
    private ActionTriggerReq actionTriggerReq;


    /**
     * 是否首尾航点（首尾航点不能是协调转弯类型）
     */
    private boolean isStartEndPoint;

}
