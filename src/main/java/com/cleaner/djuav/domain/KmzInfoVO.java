package com.cleaner.djuav.domain;

import com.cleaner.djuav.domain.kml.KmlInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * Author:Cleaner
 * Date: 2024/12/22 10:46
 **/
@Data
public class KmzInfoVO implements Serializable {

    /**
     * 航线kml信息
     */
    private KmlInfo kmlInfo;

    /**
     * 航线wpml信息
     */
    private KmlInfo wpmlInfo;

}
