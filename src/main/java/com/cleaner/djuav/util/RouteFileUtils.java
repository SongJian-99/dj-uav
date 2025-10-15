package com.cleaner.djuav.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cleaner.djuav.constant.FileTypeConstants;
import com.cleaner.djuav.domain.*;
import com.cleaner.djuav.domain.kml.*;
import com.cleaner.djuav.enums.kml.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 航线文件操作工具类
 */
public class RouteFileUtils {

    /**
     * XML 头部
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /**
     * 生成的本地 kmz 文件存储路径
     */
    private static final String LOCAL_KMZ_FILE_PATH = "file" + File.separator + "kmz" + File.separator;


    /**
     * kml文件解析
     *
     * @param inputStream
     * @return KmlInfo
     */
    public static KmlInfo parseKml(InputStream inputStream) {
        XStream xStream = new XStream();
        xStream.allowTypes(new Class[]{KmlInfo.class, KmlAction.class, KmlWayLineCoordinateSysParam.class, KmlPoint.class, KmlActionGroup.class});
        xStream.alias("kml", KmlInfo.class);
        xStream.processAnnotations(KmlInfo.class);
        xStream.autodetectAnnotations(true);
        xStream.ignoreUnknownElements();
        xStream.addImplicitCollection(KmlActionGroup.class, "action");
        xStream.addImplicitCollection(KmlPlacemark.class, "actionGroup");
        return (KmlInfo) xStream.fromXML(inputStream);
    }

    /**
     * 生成航线 KMZ 文件
     *
     * @param fileName  文件名
     * @param kmlParams 参数对象
     * @return 本地文件路径
     */
    public static String buildKmz(String fileName, KmlParams kmlParams) {
        KmlInfo kmlInfo = buildKml(kmlParams);
        KmlInfo wpmlInfo = buildWpml(kmlParams);
        return buildKmz(fileName, kmlInfo, wpmlInfo);
    }

    /**
     * 生成航线 KMZ 文件
     *
     * @param fileName 文件名
     * @param kmlInfo  kml 文件信息
     * @param wpmlInfo wpml 文件信息
     * @return 本地文件路径
     */
    public static String buildKmz(String fileName, KmlInfo kmlInfo, KmlInfo wpmlInfo) {
        XStream xStream = new XStream(new DomDriver());
        xStream.processAnnotations(KmlInfo.class);
        xStream.addImplicitCollection(KmlActionGroup.class, "action");
        xStream.addImplicitCollection(KmlPlacemark.class, "actionGroup");

        String kml = XML_HEADER + xStream.toXML(kmlInfo);
        String wpml = XML_HEADER + xStream.toXML(wpmlInfo);

        try (FileOutputStream fileOutputStream = new FileOutputStream(LOCAL_KMZ_FILE_PATH + fileName + ".kmz");
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            zipOutputStream.setLevel(0); // 0 表示不压缩，存储方式

            // 创建 wpmz 目录中的 template.kml 文件条目
            buildZipFile("wpmz/template.kml", zipOutputStream, kml);

            // 创建 wpmz 目录中的 waylines.wpml 文件条目
            buildZipFile("wpmz/waylines.wpml", zipOutputStream, wpml);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return LOCAL_KMZ_FILE_PATH + fileName + ".kmz";
    }

    private static void buildZipFile(String name, ZipOutputStream zipOutputStream, String content) throws IOException {
        ZipEntry kmlEntry = new ZipEntry(name);
        zipOutputStream.putNextEntry(kmlEntry);
        // 将内容写入 ZIP 条目
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                zipOutputStream.write(buffer, 0, length);
            }
        }
        zipOutputStream.closeEntry(); // 关闭条目
    }


    public static KmlInfo buildKml(KmlParams kmlParams) {
        KmlInfo kmlInfo = new KmlInfo();
        kmlInfo.setDocument(buildKmlDocument(FileTypeConstants.KML, kmlParams));
        return kmlInfo;
    }

    public static KmlInfo buildWpml(KmlParams kmlParams) {
        KmlInfo kmlInfo = new KmlInfo();
        kmlInfo.setDocument(buildKmlDocument(FileTypeConstants.WPML, kmlParams));
        return kmlInfo;
    }

    public static KmlDocument buildKmlDocument(String fileType, KmlParams kmlParams) {
        KmlDocument kmlDocument = new KmlDocument();
        if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
            kmlDocument.setAuthor("Cleaner");
            kmlDocument.setCreateTime(String.valueOf(DateUtil.current()));
            kmlDocument.setUpdateTime(String.valueOf(DateUtil.current()));
        }
        kmlDocument.setKmlMissionConfig(buildKmlMissionConfig(kmlParams));
        kmlDocument.setFolder(buildKmlFolder(fileType, kmlParams));
        return kmlDocument;
    }

    public static KmlMissionConfig buildKmlMissionConfig(KmlParams kmlParams) {
        KmlMissionConfig kmlMissionConfig = new KmlMissionConfig();
        kmlMissionConfig.setFlyToWayLineMode(FlyToWaylineModeEnums.SAFELY.getValue());
        kmlMissionConfig.setFinishAction(kmlParams.getFinishAction());
        if (StringUtils.isNotBlank(kmlParams.getExitOnRcLostAction())) {
            kmlMissionConfig.setExitOnRCLost(ExitOnRCLostEnums.EXECUTE_LOST_ACTION.getValue());
            kmlMissionConfig.setExecuteRCLostAction(kmlParams.getExitOnRcLostAction());
        } else {
            kmlMissionConfig.setExitOnRCLost(ExitOnRCLostEnums.GO_CONTINUE.getValue());
        }
        kmlMissionConfig.setTakeOffSecurityHeight("20");
        kmlMissionConfig.setGlobalTransitionalSpeed("15");
        kmlMissionConfig.setGlobalRTHHeight("100");
        kmlMissionConfig.setTakeOffRefPoint(kmlParams.getTakeOffRefPoint());
        kmlMissionConfig.setDroneInfo(buildKmlDroneInfo(kmlParams.getDroneType(), kmlParams.getSubDroneType()));
        kmlMissionConfig.setPayloadInfo(buildKmlPayloadInfo(kmlParams.getPayloadType(), kmlParams.getPayloadPosition()));
        return kmlMissionConfig;
    }

    public static KmlDroneInfo buildKmlDroneInfo(Integer droneType, Integer subDroneType) {
        KmlDroneInfo kmlDroneInfo = new KmlDroneInfo();
        kmlDroneInfo.setDroneEnumValue(String.valueOf(droneType));
        if (Objects.equals(droneType, DroneEnumValueEnums.M30_M30T.getValue()) ||
                Objects.equals(droneType, DroneEnumValueEnums.M3D_M3TD.getValue()) ||
                Objects.equals(droneType, DroneEnumValueEnums.M3E_M3T_M3M.getValue())) {
            kmlDroneInfo.setDroneSubEnumValue(String.valueOf(subDroneType));
        }
        return kmlDroneInfo;
    }

    public static KmlPayloadInfo buildKmlPayloadInfo(Integer payloadType, Integer payloadPosition) {
        KmlPayloadInfo kmlPayloadInfo = new KmlPayloadInfo();
        kmlPayloadInfo.setPayloadEnumValue(String.valueOf(payloadType));
        kmlPayloadInfo.setPayloadPositionIndex(String.valueOf(payloadPosition));
        return kmlPayloadInfo;

    }


    public static KmlFolder buildKmlFolder(String fileType, KmlParams kmlParams) {
        KmlFolder kmlFolder = new KmlFolder();
        kmlFolder.setTemplateId("0");
        kmlFolder.setAutoFlightSpeed(String.valueOf(kmlParams.getAutoFlightSpeed()));
        if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
            kmlFolder.setTemplateType(kmlParams.getTemplateType());
            kmlFolder.setWaylineCoordinateSysParam(buildKmlWayLineCoordinateSysParam(kmlParams.getTemplateType(), HeightModeEnums.RELATIVE_TO_START_POINT.getValue(),
                    String.valueOf(kmlParams.getGlobalHeight())));
            kmlFolder.setPayloadParam(buildKmlPayloadParam(kmlParams));
        }
        if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
            kmlFolder.setWaylineId("0");
            kmlFolder.setExecuteHeightMode(ExecuteHeightModeEnums.RELATIVE_TO_START_POINT.getValue());
            if (CollectionUtil.isNotEmpty(kmlParams.getStartActionList())) {
                KmlActionGroup kmlActionGroup = new KmlActionGroup();
                kmlActionGroup.setAction(getKmlActionList(kmlParams.getStartActionList(), kmlParams));
                kmlFolder.setStartActionGroup(kmlActionGroup);
            }
        }
        // 航点类型模板航线配置
        if (StringUtils.equals(kmlFolder.getTemplateType(), TemplateTypeEnums.WAYPOINT.getValue())) {
            WaypointTurnReq waypointTurnReq = kmlParams.getWaypointTurnReq();
            kmlFolder.setGlobalWaypointTurnMode(waypointTurnReq.getWaypointTurnMode());
            if (StringUtils.equals(waypointTurnReq.getWaypointTurnMode(), GlobalWaypointTurnModeEnums.TO_POINT_AND_STOP_WITH_CONTINUITY_CURVATURE.getValue()) ||
                    StringUtils.equals(waypointTurnReq.getWaypointTurnMode(), GlobalWaypointTurnModeEnums.TO_POINT_AND_PASS_WITH_CONTINUITY_CURVATURE.getValue())) {
                kmlFolder.setGlobalUseStraightLine("1");
            }
            kmlFolder.setGimbalPitchMode(kmlParams.getGimbalPitchMode());
            kmlFolder.setGlobalHeight(String.valueOf(kmlParams.getGlobalHeight()));
            WaypointHeadingReq waypointHeadingReq = kmlParams.getWaypointHeadingReq();
            kmlFolder.setGlobalWaypointHeadingParam(buildKmlGlobalWaypointHeadingParam(waypointHeadingReq.getWaypointHeadingMode(), waypointHeadingReq.getWaypointHeadingAngle(), waypointHeadingReq.getWaypointPoiPoint()));
            // 构建航点
            List<RoutePointInfo> routePointList = kmlParams.getRoutePointList();
            if (CollectionUtil.isNotEmpty(routePointList)) {
                routePointList.stream()
                        .min(Comparator.comparing(RoutePointInfo::getRoutePointIndex))
                        .ifPresent(routePointInfo -> routePointInfo.setIsStartAndEndPoint(Boolean.TRUE));
                routePointList.stream()
                        .max(Comparator.comparing(RoutePointInfo::getRoutePointIndex))
                        .ifPresent(routePointInfo -> routePointInfo.setIsStartAndEndPoint(Boolean.TRUE));
                List<KmlPlacemark> kmlPlacemarkList = new ArrayList<>();
                for (RoutePointInfo routePointInfo : routePointList) {
                    kmlPlacemarkList.add(buildKmlPlacemark(routePointInfo, kmlParams, fileType));
                }
                kmlFolder.setPlacemarkList(kmlPlacemarkList);
            }
        } else {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                List<KmlPlacemark> kmlPlacemarkList = new ArrayList<>();
                kmlPlacemarkList.add(buildMappingKmlPlacemark(kmlParams));
                kmlFolder.setPlacemarkList(kmlPlacemarkList);
            } else {
                // 构建航点
                List<RoutePointInfo> routePointList = kmlParams.getRoutePointList();
                if (CollectionUtil.isNotEmpty(routePointList)) {
                    List<KmlPlacemark> kmlPlacemarkList = new ArrayList<>();
                    for (RoutePointInfo routePointInfo : routePointList) {
                        kmlPlacemarkList.add(buildKmlPlacemark(routePointInfo, kmlParams, fileType));
                    }
                    kmlFolder.setPlacemarkList(kmlPlacemarkList);
                }
            }
        }
        return kmlFolder;
    }

    /**
     * 构建航点动作
     *
     * @param actionList 航点动作列表
     * @param kmlParams
     * @return
     */
    private static List<KmlAction> getKmlActionList(List<PointActionReq> actionList, KmlParams kmlParams) {
        List<KmlAction> kmlActionList = new ArrayList<>();
        for (PointActionReq pointActionReq : actionList) {
            if (ObjectUtil.isNotNull(pointActionReq.getHoverTime())) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.HOVER.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getAircraftHeading())) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.ROTATE_YAW.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getTakePhotoType()) && ObjectUtil.equals(pointActionReq.getTakePhotoType(), 0)) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.TAKE_PHOTO.getValue(), pointActionReq, kmlParams));
            } else if ((ObjectUtil.isNotNull(pointActionReq.getGimbalYawRotateAngle())) || (ObjectUtil.isNotNull(pointActionReq.getGimbalPitchRotateAngle()))) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.GIMBAL_ROTATE.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getZoom())) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.ZOOM.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getTakePhotoType()) && ObjectUtil.equals(pointActionReq.getTakePhotoType(), 1)) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.PANO_SHOT.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getStartRecord()) && pointActionReq.getStartRecord()) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.START_RECORD.getValue(), pointActionReq, kmlParams));
            } else if (ObjectUtil.isNotNull(pointActionReq.getStopRecord()) && pointActionReq.getStopRecord()) {
                kmlActionList.add(buildKmlAction(String.valueOf(pointActionReq.getActionIndex()), ActionActuatorFuncEnums.STOP_RECORD.getValue(), pointActionReq, kmlParams));
            }
        }
        return kmlActionList;
    }

    /**
     * 构造坐标系参数信息
     *
     * @param templateType 航线模板类型
     * @param heightMode   高度模式
     * @param height       高度
     * @return 坐标系参数信息
     */
    public static KmlWayLineCoordinateSysParam buildKmlWayLineCoordinateSysParam(String templateType, String heightMode, String height) {
        KmlWayLineCoordinateSysParam kmlWayLineCoordinateSysParam = new KmlWayLineCoordinateSysParam();
        kmlWayLineCoordinateSysParam.setCoordinateMode("WGS84");
        kmlWayLineCoordinateSysParam.setHeightMode(heightMode);
        kmlWayLineCoordinateSysParam.setPositioningType(PositioningTypeEnums.GPS.getValue());
        if (StringUtils.equals(templateType, TemplateTypeEnums.MAPPING2D.getValue()) ||
                StringUtils.equals(templateType, TemplateTypeEnums.MAPPING3D.getValue()) ||
                StringUtils.equals(templateType, TemplateTypeEnums.MAPPING_STRIP.getValue())) {
            kmlWayLineCoordinateSysParam.setGlobalShootHeight(height);
            kmlWayLineCoordinateSysParam.setSurfaceFollowModeEnable("1");
            kmlWayLineCoordinateSysParam.setSurfaceRelativeHeight(height);
        }
        return kmlWayLineCoordinateSysParam;
    }

    public static KmlPayloadParam buildKmlPayloadParam(KmlParams kmlParams) {
        KmlPayloadParam kmlPayloadParam = new KmlPayloadParam();
        kmlPayloadParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
        kmlPayloadParam.setFocusMode(FocusModeEnums.FIRST_POINT.getValue());
        kmlPayloadParam.setMeteringMode(MeteringModeEnums.AVERAGE.getValue());
        // 0：不开启 1：开启
        kmlPayloadParam.setDewarpingEnable("1");
        kmlPayloadParam.setReturnMode(ReturnModeEnums.SINGLE_RETURN_STRONGEST.getValue());
        // 60000、80000、120000、160000、180000、240000
        kmlPayloadParam.setSamplingRate("240000");
        kmlPayloadParam.setScanningMode(ScanningModeEnums.REPETITIVE.getValue());
        // 0: 不上色 1: 真彩上色
        kmlPayloadParam.setModelColoringEnable("1");
        kmlPayloadParam.setImageFormat(kmlParams.getImageFormat());

        return kmlPayloadParam;
    }

    public static KmlGlobalWaypointHeadingParam buildKmlGlobalWaypointHeadingParam(String waypointHeadingMode, Double waypointHeadingAngle, String waypointPoiPoint) {
        KmlGlobalWaypointHeadingParam kmlGlobalWaypointHeadingParam = new KmlGlobalWaypointHeadingParam();
        kmlGlobalWaypointHeadingParam.setWaypointHeadingMode(waypointHeadingMode);
        if (StringUtils.equals(waypointHeadingMode, WaypointHeadingModeEnums.SMOOTH_TRANSITION.getValue())) {
            kmlGlobalWaypointHeadingParam.setWaypointHeadingAngle(String.valueOf(waypointHeadingAngle));
        }
        if (StringUtils.equals(waypointHeadingMode, WaypointHeadingModeEnums.TOWARD_POI.getValue())) {
            kmlGlobalWaypointHeadingParam.setWaypointPoiPoint(waypointPoiPoint);
        }
        kmlGlobalWaypointHeadingParam.setWaypointHeadingPathMode(WaypointHeadingPathModeEnums.FOLLOW_BAD_ARC.getValue());
        return kmlGlobalWaypointHeadingParam;

    }

    public static KmlPlacemark buildKmlPlacemark(RoutePointInfo routePointInfo, KmlParams kmlParams, String fileType) {
        KmlPlacemark kmlPlacemark = new KmlPlacemark();
        kmlPlacemark.setIsRisky("0");
        kmlPlacemark.setKmlPoint(buildKmlPoint(String.valueOf(routePointInfo.getLongitude()), String.valueOf(routePointInfo.getLatitude())));
        kmlPlacemark.setIndex(String.valueOf(routePointInfo.getRoutePointIndex()));

        handleHeight(routePointInfo, kmlParams, fileType, kmlPlacemark);
        handleSpeed(routePointInfo, kmlParams, fileType, kmlPlacemark);
        handleWaypointHeadingParam(routePointInfo, kmlParams, fileType, kmlPlacemark);
        handleWaypointTurnParam(routePointInfo, kmlParams, fileType, kmlPlacemark);
        if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
            if (ObjectUtil.isNotEmpty(routePointInfo.getGimbalPitchAngle()) && StringUtils.equals(kmlParams.getGimbalPitchMode(), GimbalPitchModeEnums.USE_POINT_SETTING.getValue())) {
                kmlPlacemark.setGimbalPitchAngle(String.valueOf(routePointInfo.getGimbalPitchAngle()));
            }
        }
        if (CollectionUtil.isNotEmpty(routePointInfo.getActionGroupList())) {
            List<KmlActionGroup> kmlActionGroupList = new ArrayList<>();
            for (ActionGroupReq actionGroupReq : routePointInfo.getActionGroupList()) {
                kmlActionGroupList.add(buildKmlActionGroup(actionGroupReq, kmlParams));
            }
            kmlPlacemark.setActionGroup(kmlActionGroupList);
        }
        return kmlPlacemark;
    }

    private static KmlPlacemark buildMappingKmlPlacemark(KmlParams kmlParams) {
        KmlPlacemark kmlPlacemark = new KmlPlacemark();
        MappingTypeReq mappingTypeReq = kmlParams.getMappingTypeReq();
        kmlPlacemark.setCaliFlightEnable("0");
        kmlPlacemark.setElevationOptimizeEnable(String.valueOf(mappingTypeReq.getElevationOptimizeEnable()));
        kmlPlacemark.setSmartObliqueEnable("0");
        kmlPlacemark.setShootType(mappingTypeReq.getShootType());
        kmlPlacemark.setDirection(mappingTypeReq.getDirection());
        kmlPlacemark.setMargin(mappingTypeReq.getMargin());
        kmlPlacemark.setOverlap(buildKmlOverlap(mappingTypeReq.getCollectionMethod(), mappingTypeReq.getLensType(), mappingTypeReq.getOverlapH(), mappingTypeReq.getOverlapW()));
        kmlPlacemark.setEllipsoidHeight(String.valueOf(kmlParams.getGlobalHeight()));
        kmlPlacemark.setHeight(String.valueOf(kmlParams.getGlobalHeight()));
        kmlPlacemark.setFacadeWaylineEnable("0");
        kmlPlacemark.setPolygon(buildKmlPolygon(mappingTypeReq.getCoordinates()));
        return kmlPlacemark;
    }

    private static KmlOverlap buildKmlOverlap(String collectionMethod, String lensType, Integer overlapH, Integer overlapW) {
        KmlOverlap overlap = new KmlOverlap();
        if (StringUtils.equals(collectionMethod, LensTypeEnums.CAMERA.getValue())) {
            overlap.setOrthoCameraOverlapH(String.valueOf(overlapH));
            overlap.setOrthoCameraOverlapW(String.valueOf(overlapW));
            overlap.setInclinedCameraOverlapW(String.valueOf(overlapW));
            overlap.setInclinedCameraOverlapH(String.valueOf(overlapH));
        } else {
            overlap.setOrthoLidarOverlapH(String.valueOf(overlapH));
            overlap.setOrthoLidarOverlapW(String.valueOf(overlapW));
            overlap.setInclinedLidarOverlapH(String.valueOf(overlapH));
            overlap.setInclinedLidarOverlapW(String.valueOf(overlapW));
        }
//        if (StringUtils.equals(collectionMethod, CollectionMethodEnums.ORTHO.getValue())) {
//            if (StringUtils.equals(lensType, LensTypeEnums.LIDAR.getValue())) {
//                overlap.setOrthoLidarOverlapH(String.valueOf(overlapH));
//                overlap.setOrthoLidarOverlapW(String.valueOf(overlapW));
//            } else {
//                overlap.setOrthoCameraOverlapH(String.valueOf(overlapH));
//                overlap.setOrthoCameraOverlapW(String.valueOf(overlapW));
//            }
//        } else {
//            if (StringUtils.equals(lensType, LensTypeEnums.LIDAR.getValue())) {
//                overlap.setInclinedLidarOverlapH(String.valueOf(overlapH));
//                overlap.setInclinedLidarOverlapW(String.valueOf(overlapW));
//            } else {
//                overlap.setInclinedCameraOverlapH(String.valueOf(overlapH));
//                overlap.setInclinedCameraOverlapW(String.valueOf(overlapW));
//            }
//        }
        return overlap;
    }

    private static KmlPolygon buildKmlPolygon(List<CoordinatePointReq> coordinatePointReqList) {
        KmlPolygon kmlPolygon = new KmlPolygon();
        KmlLinearRing kmlLinearRing = new KmlLinearRing();

        String coordinates = coordinatePointReqList.stream().map(point -> point.getLongitude() + "," + point.getLatitude() + "," + point.getHeight())
                .collect(Collectors.joining(", "));
        kmlLinearRing.setCoordinates(StringUtils.join(coordinates, " "));
        KmlOuterBoundaryIs kmlOuterBoundaryIs = new KmlOuterBoundaryIs();
        kmlOuterBoundaryIs.setLinearRing(kmlLinearRing);
        kmlPolygon.setOuterBoundaryIs(kmlOuterBoundaryIs);
        return kmlPolygon;
    }

    private static void handleWaypointTurnParam(RoutePointInfo routePointInfo, KmlParams kmlParams, String fileType, KmlPlacemark kmlPlacemark) {
        WaypointTurnReq waypointTurnReq = routePointInfo.getWaypointTurnReq();
        // 使用全局航点转弯模式
        if (ObjectUtil.isNotEmpty(waypointTurnReq)) {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalTurnParam("0");
            }
            kmlPlacemark.setWaypointTurnParam(buildKmlWaypointTurnParam(waypointTurnReq.getWaypointTurnMode(),
                    waypointTurnReq.getWaypointTurnDampingDist(), waypointTurnReq.getUseStraightLine(), routePointInfo.getIsStartAndEndPoint()));
            if (ObjectUtil.isNotEmpty(waypointTurnReq.getUseStraightLine())) {
                kmlPlacemark.setUseStraightLine(String.valueOf(waypointTurnReq.getUseStraightLine()));
            }
        } else {
            // 使用自定义航点转弯模式
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalTurnParam("1");
            } else if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
                WaypointTurnReq globalWaypoint = kmlParams.getWaypointTurnReq();
                kmlPlacemark.setWaypointTurnParam(buildKmlWaypointTurnParam(globalWaypoint.getWaypointTurnMode(),
                        globalWaypoint.getWaypointTurnDampingDist(), globalWaypoint.getUseStraightLine(), routePointInfo.getIsStartAndEndPoint()));
            }
        }
    }

    private static void handleWaypointHeadingParam(RoutePointInfo routePointInfo, KmlParams kmlParams, String fileType, KmlPlacemark kmlPlacemark) {
        WaypointHeadingReq waypointHeadingReq = routePointInfo.getWaypointHeadingReq();
        if (ObjectUtil.isNotEmpty(waypointHeadingReq)) {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalHeadingParam("0");
            }
            kmlPlacemark.setWaypointHeadingParam(buildKmlWaypointHeadingParam(waypointHeadingReq.getWaypointHeadingMode(), waypointHeadingReq.getWaypointHeadingAngle(), waypointHeadingReq.getWaypointPoiPoint()));
        } else {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalHeadingParam("1");
            } else if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
                WaypointHeadingReq globalWaypointHeading = kmlParams.getWaypointHeadingReq();
                kmlPlacemark.setWaypointHeadingParam(buildKmlWaypointHeadingParam(globalWaypointHeading.getWaypointHeadingMode(), globalWaypointHeading.getWaypointHeadingAngle(), globalWaypointHeading.getWaypointPoiPoint()));
            }
        }

    }


    private static void handleSpeed(RoutePointInfo routePointInfo, KmlParams kmlParams, String fileType, KmlPlacemark kmlPlacemark) {
        if (ObjectUtil.isNotEmpty(routePointInfo.getSpeed())) {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalSpeed("0");
            }
            kmlPlacemark.setWaypointSpeed(String.valueOf(routePointInfo.getSpeed()));
        } else {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalSpeed("1");
            } else if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
                kmlPlacemark.setWaypointSpeed(String.valueOf(kmlParams.getAutoFlightSpeed()));
            }
        }
    }

    private static void handleHeight(RoutePointInfo routePointInfo, KmlParams kmlParams, String fileType, KmlPlacemark kmlPlacemark) {
        if (ObjectUtil.isNotEmpty(routePointInfo.getHeight())) {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalHeight("0");
                kmlPlacemark.setEllipsoidHeight(String.valueOf(routePointInfo.getHeight()));
                kmlPlacemark.setHeight(String.valueOf(routePointInfo.getHeight()));
            } else if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
                kmlPlacemark.setExecuteHeight(String.valueOf(routePointInfo.getHeight()));
            }
        } else {
            if (StringUtils.equals(fileType, FileTypeConstants.KML)) {
                kmlPlacemark.setUseGlobalHeight("1");
            } else if (StringUtils.equals(fileType, FileTypeConstants.WPML)) {
                kmlPlacemark.setExecuteHeight(String.valueOf(kmlParams.getGlobalHeight()));
            }
        }
    }

    public static KmlPoint buildKmlPoint(String longitude, String latitude) {
        KmlPoint kmlPoint = new KmlPoint();
        kmlPoint.setCoordinates(longitude + "," + latitude);
        return kmlPoint;
    }

    public static KmlWaypointHeadingParam buildKmlWaypointHeadingParam(String waypointHeadingMode, Double waypointHeadingAngle, String waypointPoiPoint) {
        KmlWaypointHeadingParam kmlWaypointHeadingParam = new KmlWaypointHeadingParam();
        kmlWaypointHeadingParam.setWaypointHeadingMode(waypointHeadingMode);
        if (StringUtils.equals(waypointHeadingMode, WaypointHeadingModeEnums.SMOOTH_TRANSITION.getValue())) {
            kmlWaypointHeadingParam.setWaypointHeadingAngle(String.valueOf(waypointHeadingAngle));
        }
        if (StringUtils.equals(waypointHeadingMode, WaypointHeadingModeEnums.TOWARD_POI.getValue())) {
            kmlWaypointHeadingParam.setWaypointPoiPoint(waypointPoiPoint);
        }
        kmlWaypointHeadingParam.setWaypointHeadingPathMode(WaypointHeadingPathModeEnums.FOLLOW_BAD_ARC.getValue());
        return kmlWaypointHeadingParam;
    }

    public static KmlWaypointTurnParam buildKmlWaypointTurnParam(String waypointTurnMode, Double waypointTurnDampingDist, Integer useStraightLine, Boolean startAndEndPoint) {
        KmlWaypointTurnParam kmlWaypointTurnParam = new KmlWaypointTurnParam();
        // 首尾航点不能是协调转弯类型
        if (startAndEndPoint && StringUtils.equals(waypointTurnMode, GlobalWaypointTurnModeEnums.COORDINATE_TURN.getValue())) {
            kmlWaypointTurnParam.setWaypointTurnMode(GlobalWaypointTurnModeEnums.TO_POINT_AND_STOP_WITH_DISCONTINUITY_CURVATURE.getValue());
        } else {
            kmlWaypointTurnParam.setWaypointTurnMode(waypointTurnMode);
        }
        if ((StringUtils.equals(waypointTurnMode, GlobalWaypointTurnModeEnums.COORDINATE_TURN.getValue()) ||
                StringUtils.equals(waypointTurnMode, GlobalWaypointTurnModeEnums.TO_POINT_AND_PASS_WITH_CONTINUITY_CURVATURE.getValue())) &&
                ObjectUtil.equals(useStraightLine, 1)) {
            kmlWaypointTurnParam.setWaypointTurnDampingDist(String.valueOf(waypointTurnDampingDist));
        }
        return kmlWaypointTurnParam;
    }

    public static KmlActionGroup buildKmlActionGroup(ActionGroupReq actionGroupReq, KmlParams kmlParams) {
        KmlActionGroup kmlActionGroup = new KmlActionGroup();
        kmlActionGroup.setActionGroupId(String.valueOf(actionGroupReq.getActionGroupId()));
        kmlActionGroup.setActionGroupStartIndex(String.valueOf(actionGroupReq.getActionGroupStartIndex()));
        kmlActionGroup.setActionGroupEndIndex(String.valueOf(actionGroupReq.getActionGroupEndIndex()));
        kmlActionGroup.setActionGroupMode(ActionGroupModeEnums.SEQUENCE.getValue());

        kmlActionGroup.setActionTrigger(buildKmlActionTrigger(actionGroupReq.getActionTriggerType(), actionGroupReq.getActionTriggerParam()));
        kmlActionGroup.setAction(getKmlActionList(actionGroupReq.getActions(), kmlParams));
        return kmlActionGroup;
    }

    public static KmlActionTrigger buildKmlActionTrigger(String actionTriggerType, Double actionTriggerParam) {
        KmlActionTrigger kmlActionTrigger = new KmlActionTrigger();
        kmlActionTrigger.setActionTriggerType(actionTriggerType);
        if (StringUtils.equals(actionTriggerType, ActionTriggerTypeEnums.MULTIPLE_TIMING.getValue()) ||
                StringUtils.equals(actionTriggerType, ActionTriggerTypeEnums.MULTIPLE_DISTANCE.getValue())) {
            kmlActionTrigger.setActionTriggerParam(String.valueOf(actionTriggerParam));
        }
        return kmlActionTrigger;
    }

    public static KmlAction buildKmlAction(String actionId, String actionActuatorFunc, PointActionReq pointActionReq, KmlParams kmlParams) {
        KmlAction kmlAction = new KmlAction();
        kmlAction.setActionId(actionId);
        kmlAction.setActionActuatorFunc(actionActuatorFunc);
        kmlAction.setActionActuatorFuncParam(buildKmlActionActuatorFuncParam(actionActuatorFunc, pointActionReq, kmlParams));
        return kmlAction;
    }

    public static KmlActionActuatorFuncParam buildKmlActionActuatorFuncParam(String actionActuatorFunc, PointActionReq pointActionReq, KmlParams kmlParams) {
        KmlActionActuatorFuncParam kmlActionActuatorFuncParam = new KmlActionActuatorFuncParam();
        if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.TAKE_PHOTO.getValue()) ||
                StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.START_RECORD.getValue())) {
            kmlActionActuatorFuncParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
            kmlActionActuatorFuncParam.setFileSuffix("");
            kmlActionActuatorFuncParam.setUseGlobalPayloadLensIndex(String.valueOf(pointActionReq.getUseGlobalImageFormat()));
            if (ObjectUtil.equals(pointActionReq.getUseGlobalImageFormat(), 0)) {
                kmlActionActuatorFuncParam.setPayloadLensIndex(pointActionReq.getImageFormat());
            } else {
                kmlActionActuatorFuncParam.setPayloadLensIndex(kmlParams.getImageFormat());
            }
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.STOP_RECORD.getValue())) {
            kmlActionActuatorFuncParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.GIMBAL_ROTATE.getValue())) {
            kmlActionActuatorFuncParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
            kmlActionActuatorFuncParam.setGimbalHeadingYawBase("north");
            kmlActionActuatorFuncParam.setGimbalRotateMode("absoluteAngle");
            if (!Objects.isNull(pointActionReq.getGimbalPitchRotateAngle())) {
                kmlActionActuatorFuncParam.setGimbalPitchRotateEnable("1");
                kmlActionActuatorFuncParam.setGimbalPitchRotateAngle(String.valueOf(pointActionReq.getGimbalPitchRotateAngle()));
            } else {
                kmlActionActuatorFuncParam.setGimbalPitchRotateEnable("0");
                kmlActionActuatorFuncParam.setGimbalPitchRotateAngle("0");
            }
            kmlActionActuatorFuncParam.setGimbalRollRotateEnable("0");
            kmlActionActuatorFuncParam.setGimbalRollRotateAngle("0");
            if (!Objects.isNull(pointActionReq.getGimbalYawRotateAngle())) {
                kmlActionActuatorFuncParam.setGimbalYawRotateEnable("1");
                kmlActionActuatorFuncParam.setGimbalYawRotateAngle(String.valueOf(pointActionReq.getGimbalYawRotateAngle()));
            } else {
                kmlActionActuatorFuncParam.setGimbalYawRotateEnable("0");
                kmlActionActuatorFuncParam.setGimbalYawRotateAngle("0");
            }
            kmlActionActuatorFuncParam.setGimbalRotateTimeEnable("0");
            kmlActionActuatorFuncParam.setGimbalRotateTime("0");
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.ROTATE_YAW.getValue())) {
            kmlActionActuatorFuncParam.setAircraftHeading(String.valueOf(pointActionReq.getAircraftHeading()));
            kmlActionActuatorFuncParam.setAircraftPathMode(AircraftPathModeEnums.CLOCKWISE.getValue());
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.HOVER.getValue())) {
            kmlActionActuatorFuncParam.setHoverTime(String.valueOf(pointActionReq.getHoverTime()));
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.ZOOM.getValue())) {
            kmlActionActuatorFuncParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
            kmlActionActuatorFuncParam.setFocalLength(String.valueOf(pointActionReq.getZoom()));
        } else if (StringUtils.equals(actionActuatorFunc, ActionActuatorFuncEnums.PANO_SHOT.getValue())) {
            kmlActionActuatorFuncParam.setPayloadPositionIndex(String.valueOf(kmlParams.getPayloadPosition()));
            kmlActionActuatorFuncParam.setUseGlobalPayloadLensIndex(String.valueOf(pointActionReq.getUseGlobalImageFormat()));
            kmlActionActuatorFuncParam.setPanoShotSubMode("panoShot_360");
            if (ObjectUtil.equals(pointActionReq.getUseGlobalImageFormat(), 0)) {
                kmlActionActuatorFuncParam.setPayloadLensIndex(pointActionReq.getImageFormat());
            } else {
                kmlActionActuatorFuncParam.setPayloadLensIndex(kmlParams.getImageFormat());
            }
        }
        return kmlActionActuatorFuncParam;
    }
}
