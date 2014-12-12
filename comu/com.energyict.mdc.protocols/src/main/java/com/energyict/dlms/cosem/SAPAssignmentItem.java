package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 6/03/12
 * Time: 16:23
 */
public class SAPAssignmentItem {

    private final int sap;
    private final String logicalDeviceName;

    public SAPAssignmentItem(int sap, String logicalDeviceName) {
        this.sap = sap;
        this.logicalDeviceName = logicalDeviceName;
    }

    public int getSap() {
        return sap;
    }

    public String getLogicalDeviceName() {
        return logicalDeviceName;
    }

    public Structure toStructure() {
        return new Structure(
                new Unsigned16(sap),
                OctetString.fromString(logicalDeviceName)
        );
    }

    public static SAPAssignmentItem fromAxdrBytes(byte[] axdrBytes) throws IOException {
        try {
            final AbstractDataType abstractDataType = AXDRDecoder.decode(axdrBytes);
            if (!(abstractDataType instanceof Structure)) {
                throw new ProtocolException("Expected [" + Structure.class.getName() + "] but received [" + abstractDataType.getClass().getName() + "]!");
            }
            return fromStructure((Structure) abstractDataType);
        } catch (IOException e) {
            throw new NestedIOException(e, "Unable to create SAPAssignmentItem from bytes [" + DLMSUtils.getHexStringFromBytes(axdrBytes) + "]");
        }
    }

    public static SAPAssignmentItem fromStructure(Structure structure) throws IOException {
        if (structure.nrOfDataTypes() != 2) {
            throw new ProtocolException("Expected [2] items in SAPAssignmentItem structure but found [" + structure.nrOfDataTypes() + "]");
        }
        final AbstractDataType abstractSapAddress = structure.getDataType(0);
        if (!(abstractSapAddress instanceof Unsigned16)) {
            throw new ProtocolException("Expected [" + Unsigned16.class.getName() + "] type for SAP address but was [" + abstractSapAddress.getClass().getName() + "]");
        }
        final AbstractDataType abstractLogicalDeviceName = structure.getDataType(1);
        if (!(abstractLogicalDeviceName instanceof OctetString)) {
            throw new ProtocolException("Expected [" + OctetString.class.getName() + "] type for LogicalDeviceName but was [" + abstractLogicalDeviceName.getClass().getName() + "]");
        }
        return new SAPAssignmentItem(
                ((Unsigned16) abstractSapAddress).getValue(),
                ((OctetString) abstractLogicalDeviceName).stringValue()
        );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SAPAssignmentItem");
        sb.append("{sap=").append(sap);
        sb.append(", logicalDeviceName='").append(logicalDeviceName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
