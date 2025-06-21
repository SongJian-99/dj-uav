package com.cleaner.djuav.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.cleaner.djuav.constant.FileTypeConstants;
import com.cleaner.djuav.domain.RoutePointReq;
import com.cleaner.djuav.domain.UavRouteReq;
import com.cleaner.djuav.domain.WaypointHeadingReq;
import com.cleaner.djuav.domain.WaypointTurnReq;
import com.cleaner.djuav.domain.kml.*;
import com.cleaner.djuav.enums.kml.ExitOnRCLostEnums;
import com.cleaner.djuav.service.UavRouteService;
import com.cleaner.djuav.util.FileUtils;
import com.cleaner.djuav.util.RouteFileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Author:Cleaner
 * Date: 2024/12/22 10:36
 **/
@Service
public class UavRouteServiceImpl implements UavRouteService {

    @Override
    public void updateKmz(UavRouteReq uavRouteReq) {
        // TODO 替换本地文件路径！！！
        File file = FileUtil.file("/Users/Cleaner/Project/IdeaProjects/dj-uav/file/kmz/航线kmz文件.kmz");
        try (ArchiveInputStream archiveInputStream = new ZipArchiveInputStream(FileUtil.getInputStream(file))) {
            ArchiveEntry entry;
            KmlInfo kmlInfo = new KmlInfo();
            KmlInfo wpmlInfo = new KmlInfo();
            KmlParams kmlParams = new KmlParams();
            while (!Objects.isNull(entry = archiveInputStream.getNextEntry())) {
                String name = entry.getName();
                if (name.toLowerCase().endsWith(".kml")) {
                    kmlInfo = RouteFileUtils.parseKml(archiveInputStream);
                    buildKmlParams(kmlParams, kmlInfo);
                    this.handleRouteUpdate(kmlInfo, uavRouteReq, FileTypeConstants.KML, kmlParams);
                } else if (name.toLowerCase().endsWith(".wpml")) {
                    wpmlInfo = RouteFileUtils.parseKml(archiveInputStream);
                    this.handleRouteUpdate(wpmlInfo, uavRouteReq, FileTypeConstants.WPML, kmlParams);
                }
            }
            RouteFileUtils.buildKmz("更新航线kmz文件", kmlInfo, wpmlInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildKmlParams(KmlParams kmlParams, KmlInfo kmlInfo) {
        KmlFolder folder = kmlInfo.getDocument().getFolder();
        kmlParams.setGlobalHeight(Double.valueOf(kmlInfo.getDocument().getKmlMissionConfig().getGlobalRTHHeight()));
        kmlParams.setAutoFlightSpeed(Double.valueOf(folder.getAutoFlightSpeed()));

        WaypointHeadingReq waypointHeadingReq = new WaypointHeadingReq();
        waypointHeadingReq.setWaypointHeadingMode(folder.getGlobalWaypointHeadingParam().getWaypointHeadingMode());
        waypointHeadingReq.setWaypointHeadingAngle(StringUtils.isNotBlank(folder.getGlobalWaypointHeadingParam().getWaypointHeadingAngle()) ?
                Double.valueOf(folder.getGlobalWaypointHeadingParam().getWaypointHeadingAngle()) : null);
        waypointHeadingReq.setWaypointPoiPoint(StringUtils.isNotBlank(folder.getGlobalWaypointHeadingParam().getWaypointPoiPoint()) ? folder.getGlobalWaypointHeadingParam().getWaypointPoiPoint() : null);
        kmlParams.setWaypointHeadingReq(waypointHeadingReq);

        WaypointTurnReq waypointTurnReq = new WaypointTurnReq();
        waypointTurnReq.setWaypointTurnMode(folder.getGlobalWaypointTurnMode());
        waypointTurnReq.setUseStraightLine(StringUtils.isNotBlank(folder.getGlobalUseStraightLine()) ? Integer.valueOf(folder.getGlobalUseStraightLine()) : null);

        kmlParams.setWaypointTurnReq(waypointTurnReq);
        kmlParams.setGimbalPitchMode(folder.getGimbalPitchMode());
        kmlParams.setPayloadPosition(Integer.valueOf(kmlInfo.getDocument().getKmlMissionConfig().getPayloadInfo().getPayloadPositionIndex()));
        kmlParams.setImageFormat(folder.getPayloadParam().getImageFormat());
    }

    private void handleRouteUpdate(KmlInfo kmlInfo, UavRouteReq uavRouteReq, String fileType, KmlParams kmlParams) {
        if (StringUtils.isNotBlank(uavRouteReq.getFinishAction())) {
            kmlInfo.getDocument().getKmlMissionConfig().setFinishAction(uavRouteReq.getFinishAction());
        }
        if (StringUtils.isNotBlank(uavRouteReq.getExitOnRcLostAction())) {
            kmlInfo.getDocument().getKmlMissionConfig().setExitOnRCLost(ExitOnRCLostEnums.EXECUTE_LOST_ACTION.getValue());
            kmlInfo.getDocument().getKmlMissionConfig().setExecuteRCLostAction(uavRouteReq.getExitOnRcLostAction());
        }
        if (CollectionUtil.isNotEmpty(uavRouteReq.getRoutePointList())) {
            List<KmlPlacemark> placemarkList = new ArrayList<>();
            for (RoutePointReq routePointReq : uavRouteReq.getRoutePointList()) {
                RoutePointInfo routePointInfo = BeanUtil.copyProperties(routePointReq, RoutePointInfo.class);
                KmlPlacemark kmlPlacemark = RouteFileUtils.buildKmlPlacemark(routePointInfo, kmlParams, fileType);
                placemarkList.add(kmlPlacemark);
            }
            kmlInfo.getDocument().getFolder().setPlacemarkList(placemarkList);
        }
    }

    @Override
    public void buildKmz(UavRouteReq uavRouteReq) {
        KmlParams kmlParams = new KmlParams();
        BeanUtils.copyProperties(uavRouteReq, kmlParams);
        kmlParams.setRoutePointList(BeanUtil.copyToList(uavRouteReq.getRoutePointList(), RoutePointInfo.class));
        RouteFileUtils.buildKmz("航线kmz文件", kmlParams);
    }

    @Override
    public KmlInfo parseKmz(String fileUrl) throws IOException {
        File file = FileUtils.downloadUrlToTempFile(fileUrl);
        try (ArchiveInputStream archiveInputStream = new ZipArchiveInputStream(FileUtil.getInputStream(file))) {
            ArchiveEntry entry;
            KmlInfo kmlInfo = new KmlInfo();
            KmlInfo wpmlInfo = new KmlInfo();
            while (!Objects.isNull(entry = archiveInputStream.getNextEntry())) {
                String name = entry.getName();
                if (name.toLowerCase().endsWith(".kml")) {
                    kmlInfo = RouteFileUtils.parseKml(archiveInputStream);
                } else if (name.toLowerCase().endsWith(".wpml")) {
                    wpmlInfo = RouteFileUtils.parseKml(archiveInputStream);
                }
            }
            return kmlInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
