package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 4/01/12
 * Time: 14:17
 */
public class ModbusTCPHeader {

    /**
     * Transaction/invocation Identifier (2 Bytes):
     * This identification field is used for transaction pairing when multiple messages are
     * sent along the same TCP connection by a client without waiting for a prior response.
     */
    int transactionIdentifier;


    /**
     * Protocol Identifier (2 bytes) :
     * This field is always 0 for Modbus services and other values are reserved for future extensions.
     */
    int protocolIdentifier = 0x00;

    /**
     * Length (2 bytes):
     * This field is a byte count of the unit identifier byte, the function code byte and the data fields.
     */
    int lengthField;

    /**
     * Unit Identifier (1 byte):
     * This field is used to identify a remote server located on a non TCP/IP network (for serial bridging).
     * If set to 0xFF, the slave address parameter is not used and will be ignored by the device.
     */
    int unitIdentifier = 0xFF;

    public ModbusTCPHeader() {
    }

    public ModbusTCPHeader(RequestData requestData, int transactionIdentifier, int unitIdentifier) {
        this.transactionIdentifier = transactionIdentifier;
        this.lengthField = 1 + requestData.getFrameData().length;
        this.unitIdentifier = unitIdentifier;
    }

    public ModbusTCPHeader(byte[] headerBytes) throws ModbusException {
        if (headerBytes.length != 7) {
            throw new ModbusException("The Modbus TCP/IP Header has a Wrong length. - Parsed header: " + headerBytes.toString() + ".");
        } else {
            this.transactionIdentifier = ProtocolTools.getIntFromBytes(headerBytes, 0, 2);
            this.protocolIdentifier = ProtocolTools.getIntFromBytes(headerBytes, 2, 2);
            this.lengthField = ProtocolTools.getIntFromBytes(headerBytes, 4, 2);
            this.unitIdentifier = ProtocolTools.getIntFromBytes(headerBytes, 6, 1);
        }
    }

    public ModbusTCPHeader(int transactionIdentifier, int protocolIdentifier, int lenght, int unitIdentifier) {
        this.transactionIdentifier = transactionIdentifier;
        this.protocolIdentifier = protocolIdentifier;
        this.lengthField = lenght;
        this.unitIdentifier = unitIdentifier;
    }

    public byte[] getHeaderBytes() {
        byte[] temp = ProtocolUtils.concatByteArrays(ProtocolTools.getBytesFromInt(transactionIdentifier, 2),
                ProtocolTools.getBytesFromInt(protocolIdentifier, 2));
        temp = ProtocolUtils.concatByteArrays(temp,
                ProtocolTools.getBytesFromInt(lengthField, 2));
        temp = ProtocolUtils.concatByteArrays(temp,
                ProtocolTools.getBytesFromInt(unitIdentifier, 1));
        return temp;

    }

    public int getTransactionIdentifier() {
        return transactionIdentifier;
    }

    public int getProtocolIdentifier() {
        return protocolIdentifier;
    }

    public int getLengthField() {
        return lengthField;
    }

    public int getUnitIdentifier() {
        return unitIdentifier;
    }
}