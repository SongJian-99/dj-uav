package com.cleaner.djuav.domain.kml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("Placemark")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KmlPlacemark {

    @XStreamAlias("wpml:isRisky")
    private String isRisky;

    @XStreamAlias("Point")
    private KmlPoint kmlPoint;

    @XStreamAlias("wpml:index")
    private String index;

    @XStreamAlias("wpml:useGlobalHeight")
    private String useGlobalHeight;

    @XStreamAlias("wpml:ellipsoidHeight")
    private String ellipsoidHeight;

    @XStreamAlias("wpml:height")
    private String height;

    @XStreamAlias("wpml:useGlobalSpeed")
    private String useGlobalSpeed;

    @XStreamAlias("wpml:waypointSpeed")
    private String waypointSpeed;

    @XStreamAlias("wpml:useGlobalHeadingParam")
    private String useGlobalHeadingParam;

    @XStreamAlias("wpml:waypointHeadingParam")
    private KmlWaypointHeadingParam waypointHeadingParam;

    @XStreamAlias("wpml:useGlobalTurnParam")
    private String useGlobalTurnParam;

    @XStreamAlias("wpml:waypointTurnParam")
    private KmlWaypointTurnParam waypointTurnParam;

    @XStreamAlias("wpml:useStraightLine")
    private String useStraightLine;

    @XStreamAlias("wpml:gimbalPitchAngle")
    private String gimbalPitchAngle;

    @XStreamAlias("wpml:actionGroup")
    private List<KmlActionGroup> actionGroup;

    // 下面 wpml 文件使用
    @XStreamAlias("wpml:executeHeight")
    private String executeHeight;

//    @XStreamAlias("wpml:waypointGimbalHeadingParam")
//    private KmlWaypointGimbalHeadingParam waypointGimbalHeadingParam;

    // 建图航拍模板元素
    @XStreamAlias("wpml:caliFlightEnable")
    private String caliFlightEnable;

    @XStreamAlias("wpml:elevationOptimizeEnable")
    private String elevationOptimizeEnable;

    @XStreamAlias("wpml:smartObliqueEnable")
    private String smartObliqueEnable;

    @XStreamAlias("wpml:smartObliqueGimbalPitch")
    private String smartObliqueGimbalPitch;

    @XStreamAlias("wpml:shootType")
    private String shootType;

    @XStreamAlias("wpml:direction")
    private String direction;

    @XStreamAlias("wpml:margin")
    private String margin;

    @XStreamAlias("wpml:overlap")
    private KmlOverlap overlap;

    @XStreamAlias("wpml:ovefacadeWaylineEnablerlap")
    private String facadeWaylineEnable;

    @XStreamAlias("Polygon")
    private KmlPolygon polygon;

    @XStreamAlias("wpml:mappingHeadingParam")
    private KmlMappingHeadingParam mappingHeadingParam;

    @XStreamAlias("wpml:gimbalPitchMode")
    private String gimbalPitchMode;

    @XStreamAlias("wpml:quickOrthoMappingEnable")
    private String quickOrthoMappingEnable;

    @XStreamAlias("wpml:quickOrthoMappingEnable")
    private String quickOrthoMappingPitch;

    // 倾斜摄影模板元素
    @XStreamAlias("wpml:inclinedGimbalPitch")
    private String inclinedGimbalPitch;

    @XStreamAlias("wpml:inclinedFlightSpeed")
    private String inclinedFlightSpeed;

    // 航带飞行模板元素
    @XStreamAlias("wpml:singleLineEnable")
    private String singleLineEnable;

    @XStreamAlias("wpml:cuttingDistance")
    private String cuttingDistance;

    @XStreamAlias("wpml:boundaryOptimEnable")
    private String boundaryOptimEnable;

    @XStreamAlias("wpml:leftExtend")
    private String leftExtend;

    @XStreamAlias("wpml:rightExtend")
    private String rightExtend;

    @XStreamAlias("wpml:includeCenterEnable")
    private String includeCenterEnable;

    @XStreamAlias("wpml:stripUseTemplateAltitude")
    private String stripUseTemplateAltitude;

    @XStreamAlias("LineString")
    private KmlLineString lineString;


}
