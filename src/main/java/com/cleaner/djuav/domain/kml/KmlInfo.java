package com.cleaner.djuav.domain.kml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@Data
@XStreamAlias("kml")
public class KmlInfo {

    @XStreamAsAttribute
    @XStreamAlias("xmlns")
    private String xmlns = "http://www.opengis.net/kml/2.2";

    @XStreamAsAttribute
    @XStreamAlias("xmlns:wpml")
    private String wpml = "http://www.dji.com/wpmz/1.0.6";

    @XStreamAlias("Document")
    private KmlDocument document;

}
