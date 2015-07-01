package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:30
 */
@XmlRootElement
public class RTU3ClockSyncConfiguration {

    private boolean setClock;
    private int minTimeDiff;
    private int maxTimeDiff;

    public RTU3ClockSyncConfiguration(boolean setClock, int minTimeDiff, int maxTimeDiff) {
        this.setClock = setClock;
        this.minTimeDiff = minTimeDiff;
        this.maxTimeDiff = maxTimeDiff;
    }

    //JSon constructor
    private RTU3ClockSyncConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new BooleanObject(isSetClock()));
        structure.addDataType(new Unsigned16(getMinTimeDiff()));
        structure.addDataType(new Unsigned16(getMaxTimeDiff()));
        return structure;
    }

    @XmlAttribute
    public boolean isSetClock() {
        return setClock;
    }

    @XmlAttribute
    public int getMinTimeDiff() {
        return minTimeDiff;
    }

    @XmlAttribute
    public int getMaxTimeDiff() {
        return maxTimeDiff;
    }
}