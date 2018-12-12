package com.energyict.protocolimpl.dlms.edp.logbooks;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.edp.CX20009;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 10:24
 * Author: khe
 */
public abstract class AbstractLogbookParser {

    private final CX20009 protocol;

    public AbstractLogbookParser(CX20009 protocol) {
        this.protocol = protocol;
    }

    public List<MeterEvent> readMeterEvents(Calendar from, Calendar to) throws IOException {
        List<MeterEvent> result = new ArrayList<MeterEvent>();

        Array array;
        try {
            array = getBufferData(from, to);
        } catch (DataAccessResultException e) { //Move on in case of object undefined
            protocol.getLogger().warning("Error while reading out logbook " + getObisCode() + ": " + e.getMessage());
            return result;
        }
        for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
            Structure structure = abstractDataType.getStructure();
            Date time = getTime(structure);
            int eventCode = getEventCode(structure);
            EventInfo eventInfo = getEventInfo(eventCode);
            result.add(new MeterEvent(time, eventInfo.getEisEventCode(), eventCode, eventInfo.getDescription() + getExtraDescription(structure)));
        }
        return result;
    }

    protected String getExtraDescription(Structure structure) {
        return "";  //Subclasses can override this
    }

    protected EventInfo getEventInfo(int eventCode) {
        EventInfo eventInfo = getDescriptions().get(eventCode);
        if (eventInfo == null) {
            eventInfo = new EventInfo(MeterEvent.OTHER, "Unknown event");
        }
        return eventInfo;
    }

    protected Array getBufferData(Calendar from, Calendar to) throws IOException {
        byte[] bufferData = getProtocol().getCosemObjectFactory().getProfileGeneric(getObisCode()).getBufferData(from, to);
        return new Array(bufferData, 0, 0);
    }

    protected abstract Map<Integer, EventInfo> getDescriptions();

    protected abstract ObisCode getObisCode();

    protected int getEventCode(Structure structure) {
        return structure.getDataType(1).intValue();
    }

    public CX20009 getProtocol() {
        return protocol;
    }

    protected Date getTime(Structure structure) {
        return structure.getDataType(0).getOctetString().getDateTime(protocol.getTimeZone()).getValue().getTime();
    }
}