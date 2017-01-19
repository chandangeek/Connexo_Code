package com.energyict.mdc.rest.impl.comserver;


import com.energyict.mdc.ports.ComPortType;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ComPortTypeInfo {
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
    public ComPortType id;
    public String localizedValue;

    public ComPortTypeInfo() {
    }

    public ComPortTypeInfo(ComPortType comPortType) {
        this.id = comPortType;
        // the localizedValue value should be overwritten by the actually translated value in the correspondent factory class
        if (comPortType != null) {
            this.localizedValue = new ComPortTypeAdapter().marshal(comPortType);
        }
    }

}
