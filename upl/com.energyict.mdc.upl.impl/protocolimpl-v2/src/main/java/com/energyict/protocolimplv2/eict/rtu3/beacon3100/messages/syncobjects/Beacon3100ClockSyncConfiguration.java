package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:30
 */
@XmlRootElement
public class Beacon3100ClockSyncConfiguration {
	
	/**
	 * Creates a new {@link Beacon3100ClockSyncConfiguration} by parsing the given {@link Structure}.
	 * 
	 * @param 		structure		The {@link Structure} to parse.
	 * 
	 * @return		The parsed {@link Beacon3100ClockSyncConfiguration}.
	 * 
	 * @throws 		IOException		If an error occurs parsing the data.
	 */
	public static final Beacon3100ClockSyncConfiguration fromStructure(final Structure structure) throws IOException {
		final boolean setClock = structure.getDataType(0, BooleanObject.class).getState();
		final int minDiff = structure.getDataType(1, Unsigned16.class).intValue();
		final int maxDiff = structure.getDataType(2, Unsigned16.class).intValue();
		
		return new Beacon3100ClockSyncConfiguration(setClock, minDiff, maxDiff);
	}

    private boolean setClock;
    private int minTimeDiff;
    private int maxTimeDiff;

    public Beacon3100ClockSyncConfiguration(boolean setClock, int minTimeDiff, int maxTimeDiff) {
        this.setClock = setClock;
        this.minTimeDiff = minTimeDiff;
        this.maxTimeDiff = maxTimeDiff;
    }

    //JSon constructor
    private Beacon3100ClockSyncConfiguration() {
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