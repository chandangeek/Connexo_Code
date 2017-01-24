package com.energyict.dlms.cosem;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 6/03/12
 * Time: 16:23
 */
public class SAPAssignmentItem {

    private final int sap;
    private final byte[] logicalDeviceName;

    public SAPAssignmentItem(int sap, byte[] logicalDeviceName) {
        this.sap = sap;
        this.logicalDeviceName = logicalDeviceName;
    }

    public int getSap() {
        return sap;
    }

    /**
     * Only use this if the bytes represent ASCII characters
     */
    public String getLogicalDeviceName() {
        return new String(logicalDeviceName);
    }

    public byte[] getLogicalDeviceNameBytes() {
        return logicalDeviceName;
    }

    public Structure toStructure() {
        return new Structure(
                new Unsigned16(sap),
                OctetString.fromByteArray(logicalDeviceName)
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
                ((OctetString) abstractLogicalDeviceName).getOctetStr()
        );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SAPAssignmentItem");
        sb.append("{sap=").append(sap);
        sb.append(", logicalDeviceName='").append(getLogicalDeviceName()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
