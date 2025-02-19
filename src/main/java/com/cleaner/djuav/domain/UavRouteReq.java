package com.cleaner.djuav.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Author:songjian
 * Date: 2024/12/22 10:46
 **/
@Data
public class UavRouteReq implements Serializable {

    /**
     * 无人机类型
     */
    private Integer droneType;

    /**
     * 无人机子类型
     */
    private Integer subDroneType;

    /**
     * 负载类型
     */
    private Integer payloadType;

    /**
     * 负载挂载位置
     */
    private Integer payloadPosition;

    /**
     * 负载图片存储类型
     */
    private String imageFormat;

    /**
     * 航线结束动作
     */
    private String finishAction;

    /**
     * 失控动作
     */
    private String exitOnRcLostAction;

    /**
     * 全局航线高度
     */
    private Double globalHeight;

    /**
     * 全局航线飞行速度
     */
    private Double autoFlightSpeed;

    /**
     * 全局偏航角模式
     */
    private WaypointHeadingReq waypointHeadingReq;

    /**
     * 全局航点转弯模式
     */
    private WaypointTurnReq waypointTurnReq;

    /**
     * 云台俯仰角控制模式
     */
    private String gimbalPitchMode;

    /**
     * 参考起飞点
     */
    private String takeOffRefPoint;

    /**
     * 航点列表
     */
    private List<RoutePointReq> routePointList;

}
