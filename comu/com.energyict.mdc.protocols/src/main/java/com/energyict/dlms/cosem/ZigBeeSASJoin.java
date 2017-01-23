package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.ZigBeeSASJoinAttribute;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 28/09/11
 * Time: 16:01
 */
public class ZigBeeSASJoin extends AbstractCosemObject {

    public static final ObisCode DEFAULT_OBIS = ObisCode.fromString("0.0.35.1.1.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public ZigBeeSASJoin(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     */
    public ZigBeeSASJoin(ProtocolLink protocolLink) {
        this(protocolLink, new ObjectReference(DEFAULT_OBIS.getLN()));
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.ZIGBEE_SAS_JOIN.getClassId();
    }

    public Unsigned8 readScanAttempts() throws IOException {
        return new Unsigned8(getResponseData(ZigBeeSASJoinAttribute.SCAN_ATTEMPTS), 0);
    }

    public Unsigned16 readTimeBetweenScans() throws IOException {
        return new Unsigned16(getResponseData(ZigBeeSASJoinAttribute.TIME_BETWEEN_SCANS), 0);
    }

    public Unsigned16 readRejoinInterval() throws IOException {
        return new Unsigned16(getResponseData(ZigBeeSASJoinAttribute.REJOIN_INTERVAL), 0);
    }

    public Unsigned16 readMaxRejoinInterval() throws IOException {
        return new Unsigned16(getResponseData(ZigBeeSASJoinAttribute.MAX_REJOIN_INTERVAL), 0);
    }

    public void writeScanAttempts(Unsigned8 scanAttempts) throws IOException {
        write(ZigBeeSASJoinAttribute.SCAN_ATTEMPTS, scanAttempts.getBEREncodedByteArray());
    }

    public void writeTimeBetweenScans(Unsigned16 timeBetweenScans) throws IOException {
        write(ZigBeeSASJoinAttribute.TIME_BETWEEN_SCANS, timeBetweenScans.getBEREncodedByteArray());
    }

    public void writeRejoinInterval(Unsigned16 rejoinInterval) throws IOException {
        write(ZigBeeSASJoinAttribute.REJOIN_INTERVAL, rejoinInterval.getBEREncodedByteArray());
    }

    public void writeMaxRejoinInterval(Unsigned16 maxRejoinInterval) throws IOException {
        write(ZigBeeSASJoinAttribute.MAX_REJOIN_INTERVAL, maxRejoinInterval.getBEREncodedByteArray());
    }

}
