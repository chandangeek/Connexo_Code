package com.energyict.mdc.rest.impl.comserver;


import com.energyict.mdc.channel.serial.Parities;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ParitiesInfo {
    @XmlJavaTypeAdapter(ParitiesAdapter.class)
    public Parities id;
    public String localizedValue;

    public ParitiesInfo() {

    }

    public ParitiesInfo(Parities parity) {
        this.id = parity;
        // the localizedValue value should be overwritten by the actually translated value in the correspondent factory class
        if(parity != null) {
            this.localizedValue = new ParitiesAdapter().marshal(parity);
        }
    }
}
