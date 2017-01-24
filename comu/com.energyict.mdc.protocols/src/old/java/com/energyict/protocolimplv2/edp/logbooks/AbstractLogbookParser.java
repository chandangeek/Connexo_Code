package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.edp.CX20009;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 10:24
 * Author: khe
 */
public abstract class AbstractLogbookParser {

    private final CX20009 protocol;
    private final MeteringService meteringService;

    public AbstractLogbookParser(CX20009 protocol, MeteringService meteringService) {
        this.protocol = protocol;
        this.meteringService = meteringService;
    }

    public List<MeterProtocolEvent> parseEvents(byte[] bufferData) throws IOException {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        Array array = new Array(bufferData, 0, 0);
        for (AbstractDataType abstractDataType : array.getAllDataTypes()) {
            Structure structure = abstractDataType.getStructure();
            Date time = getTime(structure);
            int eventCode = getEventCode(structure);
            EventInfo eventInfo = getEventInfo(eventCode);
            result.add(new MeterEvent(time, eventInfo.getEisEventCode(), eventCode, eventInfo.getDescription() + getExtraDescription(structure)));
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(result, this.meteringService);
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

    protected abstract Map<Integer, EventInfo> getDescriptions();

    public abstract ObisCode getObisCode();

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