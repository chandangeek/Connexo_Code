package com.energyict.smartmeterprotocolimpl.elster.AS300P.eventhandling;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300PObisCodeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 11:33
 */
public class AS300PEventLog {

    private ObisCode obisCode;
    private final CosemObjectFactory cosemObjectFactory;
    private final Logger logger;

    public AS300PEventLog(ObisCode obisCode, CosemObjectFactory cosemObjectFactory, Logger logger) {
        this.obisCode = obisCode;
        this.cosemObjectFactory = cosemObjectFactory;
        this.logger = logger;
    }

    public List<MeterEvent> getEvents(Calendar from) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + getObisCode() + "] from [" + from.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Array eventArray = getEventArray(from);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            AS300PEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                meterEvents.add(basicEvent.getMeterEvent(getLogBookId()));
            }
        }

        return meterEvents;
    }

    private AS300PEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        try {
            if (abstractEventData.isStructure()) {
                Structure structure = abstractEventData.getStructure();
                return structure != null ? new AS300PEvent(structure, getLogger()) : null;
            } else {
                throw new IOException("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
            }
        } catch (IOException e) {
            throw new IOException("Unable to parse single event entry: " + e.getMessage());
        }
    }

    private Array getEventArray(Calendar from) throws IOException {
        byte[] rawData = getCosemObjectFactory().getProfileGeneric(getObisCode()).getBufferData(from);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    /**
     * Get the logBookId for this logbook. Normally we use the E field of the obisCode for this.
     * @return the logBookId
     */
    private int getLogBookId() {
        if (obisCode.equals(AS300PObisCodeProvider.PowerFailureEventLogObisCode)) {
            return 7;
        } else {
            return obisCode.getE();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }
}
