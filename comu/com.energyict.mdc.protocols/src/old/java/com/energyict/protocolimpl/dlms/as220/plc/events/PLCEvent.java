package com.energyict.protocolimpl.dlms.as220.plc.events;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Structure with the following format:
 * <p/><pre>
 * sequence {
 *      time:           octet-string    = OctetString
 *      channel:        unsigned        = Unsigned8
 *      master-address: long-unsigned   = Unsigned16
 *      rejected:       boolean         = BooleanObject
 *      s0:             long-unsigned   = Unsigned16
 *      n0:             long-unsigned   = Unsigned16
 *      s1:             long-unsigned   = Unsigned16
 *      n1:             long-unsigned   = Unsigned16
 *      gain:           unsigned        = Unsigned8
 *      method:         unsigned        = Unsigned8
 * }
 * </pre><p/>
 * Copyrights
 * Date: 17/06/11
 * Time: 13:19
 */
public class PLCEvent extends Structure {

    private final TimeZone timeZone;

    private static final int PLC_STRUCTURE_SIZE = 10;

    private static final int INDEX_TIME = 0;
    private static final int INDEX_CHANNEL = 1;
    private static final int INDEX_MASTER_ADDR = 2;
    private static final int INDEX_REJECTED = 3;
    private static final int INDEX_S0 = 4;
    private static final int INDEX_N0 = 5;
    private static final int INDEX_S1 = 6;
    private static final int INDEX_N1 = 7;
    private static final int INDEX_GAIN = 8;
    private static final int INDEX_METHOD = 9;

    private static final String[] FIELD_NAMES = new String[]{
            "TIME",
            "CHANNEL",
            "MASTER_ADDR",
            "REJECTED",
            "S0",
            "N0",
            "S1",
            "N1",
            "GAIN",
            "METHOD"
    };

    public PLCEvent(byte[] berEncodedByteArray, TimeZone timeZone) throws IOException {
        super(berEncodedByteArray, 0, 0);
        validatePLCEvent();
        this.timeZone = timeZone == null ? TimeZone.getDefault() : timeZone;
    }

    private void validatePLCEvent() throws IOException {
        if (nrOfDataTypes() != 10) {
            throw new IOException("PLCEvent should contain [" + PLC_STRUCTURE_SIZE + "] data types, but has [" + nrOfDataTypes() + "]");
        }

        int i = INDEX_TIME;
        if ((getDataType(i) == null) || (!getDataType(i).isOctetString())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an OctetString.");
        }

        i = INDEX_CHANNEL;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned8())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned8.");
        }

        i = INDEX_MASTER_ADDR;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned16())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned16.");
        }

        i = INDEX_REJECTED;
        if ((getDataType(i) == null) || (!getDataType(i).isBooleanObject())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an BooleanObject.");
        }

        i = INDEX_S0;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned16())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned16.");
        }

        i = INDEX_N0;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned16())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned16.");
        }

        i = INDEX_S1;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned16())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned16.");
        }

        i = INDEX_N1;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned16())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned16.");
        }
        i = INDEX_GAIN;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned8())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned8.");
        }

        i = INDEX_METHOD;
        if ((getDataType(i) == null) || (!getDataType(i).isUnsigned8())) {
            throw new IOException("PLCEvent '" + FIELD_NAMES[i] + "' at index [" + i + "] should be an Unsigned8.");
        }

    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Date getEventDate() {
        return getDataType(INDEX_TIME).getOctetString().getDateTime(getTimeZone()).getValue().getTime();
    }

    public int getChannel() {
        return getDataType(INDEX_CHANNEL).getUnsigned8().getValue();
    }

    public int getMasterAddress() {
        return getDataType(INDEX_MASTER_ADDR).getUnsigned16().getValue();
    }

    public boolean isRejected() {
        return getDataType(INDEX_REJECTED).getBooleanObject().getState();
    }

    public int getS0() {
        return getDataType(INDEX_S0).getUnsigned16().getValue();
    }

    public int getN0() {
        return getDataType(INDEX_N0).getUnsigned16().getValue();
    }

    public int getS1() {
        return getDataType(INDEX_S1).getUnsigned16().getValue();
    }

    public int getN1() {
        return getDataType(INDEX_N1).getUnsigned16().getValue();
    }

    public int getGain() {
        return getDataType(INDEX_GAIN).getUnsigned8().getValue();
    }

    public int getMethod() {
        return getDataType(INDEX_METHOD).getUnsigned8().getValue();
    }

    public MeterEvent getMeterEvent() {
        return new MeterEvent(getEventDate(), MeterEvent.OTHER, "PLCEvent: {" + getDescription() + "}");
    }

    public String getDescription() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CHANNEL").append("=").append(getChannel() + 1).append(", ");
        sb.append("MASTER_ADDR").append("=").append(ProtocolTools.getHexStringFromInt(getMasterAddress(),2,"")).append(", ");
        sb.append("REJECTED").append("=").append(isRejected()).append(", ");
        sb.append("S0").append("=").append(getS0()).append(", ");
        sb.append("N0").append("=").append(getN0()).append(", ");
        sb.append("S1").append("=").append(getS1()).append(", ");
        sb.append("N1").append("=").append(getN1()).append(", ");
        sb.append("GAIN").append("=").append(getGain()).append("/7, ");
        sb.append("METHOD").append("=").append(getMethodDescription());

        if (getChannel()==0 && getMasterAddress()==0 && isRejected() == false && getS0() == 0
                && getN0() == 0 && getS1() == 0 && getN1() == 0 && getGain() == 0 && getMethod()== 0) {
            return "Start of plc scan";
        } else if (isRejected() == false && getS0() == 0
                && getN0() == 0 && getS1() == 0 && getN1() == 0 && getGain() == 0 && getMethod()== 0) {
            return "End of plc scan. Selected Master Address["+ProtocolTools.getHexStringFromInt(getMasterAddress(),2,"")+"] on channel ["+   (getChannel()+1) +"]";
        }
        return sb.toString();
    }

    private String getMethodDescription() {
        int method = getMethod();
        switch (method) {
            case 1:
                return "ASK0" + " [" + method + "]";
            case 2:
                return "ASK1" + " [" + method + "]";
            case 3:
                return "FSK" + " [" + method + "]";
            case 4:
                return "FSK0" + " [" + method + "]";
            case 5:
                return "FSK1" + " [" + method + "]";
        }
        return "Unknown [" + method + "]";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PLCEvent{");
        sb.append("TIME").append("=").append(getEventDate()).append(", ");
        sb.append(getDescription());
        sb.append('}');
        return sb.toString();
    }

}
