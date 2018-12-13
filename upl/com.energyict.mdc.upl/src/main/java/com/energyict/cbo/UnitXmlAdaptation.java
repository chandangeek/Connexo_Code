package com.energyict.cbo;

import javax.xml.bind.annotation.XmlElement;

/**
 * Adapter class for {@link com.energyict.cbo.Unit} to enable xml marshalling.
 *
 * @author sva
 * @since 16/04/2014 - 16:35
 */
public class UnitXmlAdaptation {

    @XmlElement
    public int code;

    @XmlElement
    public int scale;

    public UnitXmlAdaptation() {
        super();
    }

    public UnitXmlAdaptation(Unit unit) {
        this();
        this.code = unit.getDlmsCode();
        this.scale = unit.getScale();
    }
}