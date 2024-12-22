package com.cleaner.djuav.domain.kml;

import com.cleaner.djuav.domain.RoutePointReq;
import lombok.Data;

import java.util.List;

/**
 * 航线文件
 */
@Data
public class KmlParams {
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
     * 航线结束动作
     */
    private String finishAction;

    /**
     * 失控动作
     */
    private String exitOnRcLostAction;

//    /**
//     * 参考起飞点
//     */
//    private String takeOffRefPoint;

    /**
     * 全局航点转弯模式
     */
    private String globalWaypointTurnMode;

    /**
     * 全局航线高度
     */
    private Double globalHeight;

    /**
     * 全局飞行器偏航角模式
     */
    private String waypointHeadingMode;

    /**
     * 全局飞行器偏航角度
     */
    private Double waypointHeadingAngle;

    /**
     * 兴趣点
     */
    private String waypointPoiPoint;

    /**
     * 云台俯仰角控制模式
     */
    private String gimbalPitchMode;

    /**
     * 全局航线飞行速度
     */
    private Double autoFlightSpeed;

    /**
     * 负载图片存储类型
     */
    private String imageFormat;

    /**
     * 航点列表
     */
    private List<RoutePointReq> routePointList;
}

