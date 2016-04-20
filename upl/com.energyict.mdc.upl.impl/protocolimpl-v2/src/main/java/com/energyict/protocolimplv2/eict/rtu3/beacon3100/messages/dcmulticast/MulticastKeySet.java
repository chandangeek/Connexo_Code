package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/03/2016 - 17:16
 */
@XmlRootElement
public class MulticastKeySet {

    MulticastGlobalKeySet globalKeySet;

    public MulticastKeySet(MulticastGlobalKeySet globalKeySet) {
        this.globalKeySet = globalKeySet;
    }

    //JSon constructor
    private MulticastKeySet() {
    }

    public AbstractDataType toDataType() {
        if (getGlobalKeySet() == null) {
            return new NullData();
        } else {
            return getGlobalKeySet().toStructure();
        }
    }

    @XmlAttribute
    public MulticastGlobalKeySet getGlobalKeySet() {
        return globalKeySet;
    }
}