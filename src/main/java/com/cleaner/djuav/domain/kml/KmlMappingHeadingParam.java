package com.cleaner.djuav.domain.kml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("wpml:mappingHeadingParam")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KmlMappingHeadingParam {

    @XStreamAlias("wpml:mappingHeadingMode")
    private String mappingHeadingMode;

    @XStreamAlias("wpml:actionActuatorFunc")
    private String actionActuatorFunc;
        

}
