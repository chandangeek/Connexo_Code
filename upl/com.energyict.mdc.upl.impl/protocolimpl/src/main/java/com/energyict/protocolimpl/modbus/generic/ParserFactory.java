package com.energyict.protocolimpl.modbus.generic;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.generic.common.DataType;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A custom implementation of the {@link com.energyict.protocolimpl.modbus.core.ParserFactory} class,
 * specific for the generic modbus protocol
 *
 * @author sva
 * @since 19/11/2013 16:51
 */
public class ParserFactory extends com.energyict.protocolimpl.modbus.core.ParserFactory {

    /**
     * Creates a new instance of ParserFactory
     */
    public ParserFactory() {
    }

    public Parser get(String key) throws IOException {
        DataTypeSelector dataTypeSelector = DataTypeSelector.getDataTypeSelector(Integer.parseInt(key));
        DataType dataType = dataTypeSelector.getDataType();
        if (dataType.equals(DataTypeSelector.BYTE_DATA_TYPE)
                || dataType.equals(DataTypeSelector.REGISTER_DATA_TYPE)
                || dataType.equals(DataTypeSelector.INTEGER_DATA_TYPE)
                || dataType.equals(DataTypeSelector.LONG_DATA_TYPE)) {
            return getUnsignedValueParser(dataTypeSelector.isBigEndianEncoded());
        } else if (dataType.equals(DataTypeSelector.SIGNED_REGISTER_DATA_TYPE) ||
                dataType.equals(DataTypeSelector.SIGNED_INTEGER_DATA_TYPE)
                || dataType.equals(DataTypeSelector.SIGNED_LONG_DATA_TYPE)) {
            return getSignedValueParser(dataTypeSelector.isBigEndianEncoded());
        } else if (dataType.equals(DataTypeSelector.FLOAT_32_BIT_DATA_TYPE) ||
                dataType.equals(DataTypeSelector.FLOAT_64_BIT_DATA_TYPE)) {
            return getFloatingPointParser(dataTypeSelector.isBigEndianEncoded());
        } else if (dataType.equals(DataTypeSelector.BCD_32_BIT_DATA_TYPE)
                || dataType.equals(DataTypeSelector.BCD_64_BIT_DATA_TYPE)) {
            return getBCDParser(dataTypeSelector.isBigEndianEncoded());
        } else if (dataType.equals(DataTypeSelector.MODULO10_32_BIT_DATA_TYPE)
                || dataType.equals(DataTypeSelector.MODULO10_64_BIT_DATA_TYPE)) {
            return getModulo10Parser(dataTypeSelector.isBigEndianEncoded());
        } else if (dataType.equals(DataTypeSelector.ASCII_DATA_TYPE)) {
            return getAsciiParser(dataTypeSelector.isBigEndianEncoded());
        } else {
            throw new ModbusException("ParserFactory, no parser found for key " + key + ".");
        }
    }

    private Parser getUnsignedValueParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 || values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, UnsignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, UnsignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getSignedValueParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 || values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, SignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, SignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getFloatingPointParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 2) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(Float.toString(Float.intBitsToFloat(bigInteger.intValue())));
                    } else if (values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(Double.toString(Double.longBitsToDouble(bigInteger.longValue())));
                    } else {
                        throw new ModbusException("ParserFactory, FloatingPointParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, FloatingPointParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getBCDParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        return new BigDecimal(ProtocolTools.getHexStringFromBytes(intBitsArray, ""));
                    } else {
                        throw new ModbusException("ParserFactory, BCDParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, BCDParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getModulo10Parser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 2 || values.length == 4) {
                        long val = 0;
                        for (int i = 0; i < values.length; i++) {
                            val += values[i] * (long) Math.pow(10, (bigEndianEncoding ? (values.length - 1 -i) : i) * 4);
                        }
                        return BigDecimal.valueOf(val);
                    } else {
                        throw new ModbusException("ParserFactory, Modulo10Parser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, Modulo10Parser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getAsciiParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                String asciiString = "";
                if (bigEndianEncoding) {
                    for (int i = 0; i < values.length; i++) {
                        asciiString += (char) (values[i] >> 8 & 0xFF);
                        asciiString += (char) (values[i] & 0xFF);
                    }
                } else {
                    for (int i = (values.length - 1); i >= 0; i--) {
                        asciiString += (char) (values[i] >> 8 & 0xFF);
                        asciiString += (char) (values[i] & 0xFF);
                    }
                }
                return asciiString.trim();
            }
        };
    }

    protected byte[] getByteArrayFromValue(int[] values, boolean bigEndianEncoding) {
        byte[] byteArray = new byte[values.length * 2];

        if (bigEndianEncoding) {
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                byteArray[index] = (byte) (values[i] >> 8 & 0xFF);
                byteArray[index + 1] = (byte) values[i];
                index += 2;
            }
        } else {
            int index = (values.length * 2) - 2;
            for (int i = 0; i < values.length; i++) {
                byteArray[index] = (byte) (values[i] >> 8 & 0xFF);
                byteArray[index + 1] = (byte) values[i];
                index -= 2;
            }
        }

        return byteArray;
    }

    protected int[] convertLittleToBigEndian(int[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[values.length - 1 - i];
        }
        return result;
    }
}
