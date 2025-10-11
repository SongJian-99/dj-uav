package com.cleaner.djuav.service;

import com.cleaner.djuav.domain.KmzInfoVO;
import com.cleaner.djuav.domain.UavRouteReq;

import java.io.IOException;

public interface UavRouteService {

    /**
     * 编辑kmz文件
     */
    void updateKmz(UavRouteReq uavRouteReq);

    /**
     * 生成kmz文件(带航点)
     */
    void buildKmz(UavRouteReq uavRouteReq);

    /**
     * 解析kmz文件
     *
     * @param file
     */
    KmzInfoVO parseKmz(String file) throws IOException;
}
