package com.cleaner.djuav.controller;

import com.cleaner.djuav.domain.KmzInfoVO;
import com.cleaner.djuav.domain.UavRouteReq;
import com.cleaner.djuav.service.UavRouteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class UavRouteController {

    @Resource
    private UavRouteService routeService;

    /**
     * 编辑kmz文件
     */
    @PostMapping("/updateKmz")
    public void updateKmz(@RequestBody UavRouteReq uavRouteReq) {
        this.routeService.updateKmz(uavRouteReq);
    }

    /**
     * 生成kmz文件
     */
    @PostMapping("/buildKmz")
    public void buildKmz(@RequestBody UavRouteReq uavRouteReq) {
        this.routeService.buildKmz(uavRouteReq);
    }

    /**
     * 解析kmz文件
     *
     * @param fileUrl
     */
    @PostMapping("/parseKmz")
    public KmzInfoVO parseKmz(@RequestParam("fileUrl") String fileUrl) throws IOException {
        return this.routeService.parseKmz(fileUrl);
    }
}
