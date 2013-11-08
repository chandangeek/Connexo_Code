package com.energyict.protocolimpl.din19244.poreg2.core;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing logic to parse received bytes.
 * The first byte is the data type (each type has an implicit length), followed by a value.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21-apr-2011
 * Time: 14:43:50
 */
public class RegisterDataParser {

    /**
     * This method parses the received values based on their included data type.
     *
     * @param data:              the meter response, containing a chain of data types and values.
     * @param numberOfRegisters: the number of registers in the register group
     * @param numberOfFields:    the number of fields per register
     * @return a list of values
     */
    public static List<ExtendedValue> parseData(byte[] data, int numberOfRegisters, int numberOfFields) {
        List<ExtendedValue> result = new ArrayList<ExtendedValue>();
        int offset = 0;
        for (int registerIndex = 0; registerIndex < numberOfRegisters; registerIndex++) {
            for (int fieldIndex = 0; fieldIndex < numberOfFields; fieldIndex++) {
                int dataTypeID = data[offset++] & 0xFF;
                if (dataTypeID == 0) {
                    result.add(new ExtendedValue(DataType.fromId(0), 0, false));
                    continue;            //Empty value, skip
                }
                DataType dataType = DataType.fromId(dataTypeID);            //Check the data type
                result.add(parseValue(data, offset, dataType));             //Parse the value
                offset += dataType.getLength();
            }
        }
        return result;
    }

    public static List<ExtendedValue> parseData(byte[] data, int offset, int numberOfRegisters, int numberOfFields) {
        data = ProtocolTools.getSubArray(data, offset);
        return parseData(data, numberOfRegisters, numberOfFields);
    }

    private static ExtendedValue parseValue(byte[] data, int offset, DataType dataType) {
        byte[] valueBytes = ProtocolTools.getSubArray(data, offset, offset + dataType.getLength());
        valueBytes = ProtocolTools.reverseByteArray(valueBytes);
        if (dataType.isSigned()) {
            return new ExtendedValue(dataType, ProtocolTools.getIntFromBytes(valueBytes, 0, dataType.getLength()));
        } else {
            return new ExtendedValue(dataType, ProtocolTools.getUnsignedIntFromBytes(valueBytes, 0, dataType.getLength()));
        }
    }

    public static String parseAsciiBytes(byte[] data) {
        int length = (data[0] & 0xFF) - DataType.ASCII.getId();
        data = ProtocolTools.getSubArray(data, 1, 1 + length);
        return ProtocolTools.getAsciiFromBytes(data);
    }

    public static int parseBCDBytes(byte[] data) {
        int length = (data[0] & 0xFF) - DataType.BCD.getId();
        data = ProtocolTools.getSubArray(data, 1, 1 + length);
        String value = ProtocolTools.getHexStringFromBytes(data, "");
        return Integer.parseInt(value);
    }
}