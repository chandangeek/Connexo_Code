/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.attributes.GPRSStandardStatusAttributes;

import java.io.IOException;

/**
 * @author gna
 */
public class GPRSStandardStatus extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public GPRSStandardStatus(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.GSM_STANDARD_STATUS.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.LOGICAL_NAME, OctetString.class);
    }

    public AbstractDataType readSubscriberId() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.SUBSCRIBER_ID, VisibleString.class);
    }

    public AbstractDataType readModemModel() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.MODEM_MODEL, VisibleString.class);
    }

    public AbstractDataType readModemRevision() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.MODEM_REVISION, VisibleString.class);
    }

    public AbstractDataType readModemFirmware() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.MODEM_FIRMWARE, VisibleString.class);
    }

    public AbstractDataType readModemSerialNr() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.MODEM_SERIAL_NR, VisibleString.class);
    }

    public AbstractDataType readNetworkProvider() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.NETWORK_PROVIDER, VisibleString.class);
    }

    public AbstractDataType readSignalStrength() throws IOException {
        return readDataType(GPRSStandardStatusAttributes.SIGNAL_STRENGTH, Integer16.class);
    }
}