package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 12/3/12
 * Time: 9:46 AM
 */
public class G3LqiEventLog implements EventLog {

    /**
     * The minimum number of entries in each event entry structure
     */
    private static final int MINIMUM_STRUCTURE_ENTRIES = 2;

    private final ObisCode obisCode;
    private final String name;
    private final Logger logger;
    private final TimeZone timeZone;
    private final CosemObjectFactory cosemObjectFactory;

    public G3LqiEventLog(final DlmsSession session, final ObisCode obisCode, final String name) {
        this.obisCode = obisCode;
        this.name = name;
        this.logger = session.getLogger();
        this.timeZone = session.getTimeZone();
        this.cosemObjectFactory = session.getCosemObjectFactory();
    }

    public G3LqiEventLog(CosemObjectFactory cosemObjectFactory, ObisCode obisCode, Logger logger, TimeZone timeZone) {
        this.obisCode = obisCode;
        this.name = "";
        this.logger = logger;
        this.timeZone = timeZone;
        this.cosemObjectFactory = cosemObjectFactory;
    }

    @Override
    public ObisCode getObisCode() {
        return obisCode;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public Logger getLogger() {
        return logger;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public final List<MeterEvent> getEvents(final Calendar from, final Calendar to) throws IOException {
        getLogger().log(Level.INFO, "Fetching EVENTS from [" + obisCode + "], [" + name + "] from [" + from.getTime() + "] to [" + to.getTime() + "]");

        final ProfileGeneric eventLog = getCosemObjectFactory().getProfileGeneric(obisCode);

        final byte[] rawBuffer = eventLog.getBufferData(from, to);
        final Array eventArray = AXDRDecoder.decode(rawBuffer, Array.class);
        if (eventArray == null) {
            throw new IOException("Unable to get the ProfileGeneric with obisCode [" + obisCode + "] from [" + from.getTime() + "] to [" + to.getTime() + "]! Event array was 'null'.");
        }

        final Calendar dayOfEvents = (Calendar) from.clone();
        dayOfEvents.set(Calendar.MILLISECOND, 0);
        dayOfEvents.set(Calendar.SECOND, 0);
        dayOfEvents.set(Calendar.MINUTE, 0);
        dayOfEvents.set(Calendar.HOUR_OF_DAY, 0);

        return parseEvents(eventArray);

    }

    /**
     * Generate a list of {@link com.energyict.protocol.MeterEvent} from the given event array.
     *
     * @param eventArray The event array, containing the axdr encoded events from the meter
     * @return The list of events or an empty list if no events found. Should NEVER return 'null'
     */
    public List<MeterEvent> parseEvents(AbstractDataType eventArray) throws IOException {
        final List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (final AbstractDataType dataType : (Array) eventArray) {
            try {

                final MeterEvent meterEvent = createMeterEvent(dataType);
                if (meterEvent != null) {
                    meterEvents.add(meterEvent);
                }

            } catch (IOException e) {
                final byte[] berArray = dataType.getBEREncodedByteArray();
                final String hexContent = ProtocolTools.getHexStringFromBytes(berArray, "");
                getLogger().log(Level.WARNING, "Unable to parse event entry [" + hexContent + "]: " + e.getMessage(), e);
            }
        }
        return meterEvents;
    }

    /**
     * Create a new meter event from the given values
     *
     * @param dataType The data type containing the AXDR encoded meter event (should be a structure)
     * @return The new meter event, created from the given values
     * @throws java.io.IOException If we were unable to create the event (parse error, ...)
     */
    private final MeterEvent createMeterEvent(final AbstractDataType dataType) throws IOException {
        if (!(dataType instanceof Structure)) {
            throw new IOException("Expected [Structure] but was [" + dataType.getClass().getSimpleName() + "]!");
        }

        final Structure eventStructure = (Structure) dataType;
        if (eventStructure.nrOfDataTypes() < MINIMUM_STRUCTURE_ENTRIES) {
            throw new IOException("Event needs at least [" + MINIMUM_STRUCTURE_ENTRIES + "] values, but has only [" + eventStructure.nrOfDataTypes() + "]!");
        }

        final OctetString dateTime = eventStructure.getDataType(0, OctetString.class);
        if (dateTime == null) {
            throw new IOException("OctetString containing the date/time is 'null'!");
        }

        final Unsigned8 lqiDataType = eventStructure.getDataType(1, Unsigned8.class);
        if (lqiDataType == null) {
            throw new IOException("LQI value is 'null'!");
        }

        final Calendar eventTime = buildEventTime(dateTime);
        final int lqiValue = lqiDataType.getValue();
        final String eventMessage = "LQI = [" + lqiValue + ']';

        return new MeterEvent(eventTime.getTime(), MeterEvent.OTHER, 96, eventMessage, obisCode.getE(), 96);
    }

    /**
     * Create a new {@link java.util.Calendar} representing the event time
     *
     * @param dateTime The exact time (only hours, minutes and seconds are used)
     * @return The event time, represented as a {@link java.util.Calendar}
     * @throws java.io.IOException If the timestamp could not be build
     */
    private final Calendar buildEventTime(final OctetString dateTime) throws IOException {
        OctetString eventDateString = dateTime.getOctetString();
        AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, getTimeZone());
        return timeStamp.getValue();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("G3LqiEventLog");
        sb.append("{obisCode=").append(obisCode);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
