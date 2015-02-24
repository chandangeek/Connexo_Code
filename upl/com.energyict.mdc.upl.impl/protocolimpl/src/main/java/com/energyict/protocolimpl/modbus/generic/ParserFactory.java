package com.energyict.protocolimpl.modbus.generic;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.Parser;
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
        DataTypeSelector.DataType dataType = dataTypeSelector.getDataType();
        switch (dataType) {
            case BYTE:
            case REGISTER:
            case INTEGER:
            case LONG:
                return dataTypeSelector.isBigEndianEncoded()
                        ? getUnsignedValueParser(true)
                        : getUnsignedValueParser(false);

            case SIGNED_REGISTER:
            case SIGNED_INTEGER:
            case SIGNED_LONG:
                return dataTypeSelector.isBigEndianEncoded()
                        ? getSignedValueParser(true)
                        : getSignedValueParser(false);

            case FLOAT_32BIT:
            case FLOAT_64BIT:
                return dataTypeSelector.isBigEndianEncoded()
                        ? getFloatingPointParser(true)
                        : getFloatingPointParser(false);
            case BCD_32BIT:
            case BCD_64BIT:
                return dataTypeSelector.isBigEndianEncoded()
                        ? getBCDParser(true)
                        : getBCDParser(false);
            case ASCII:
                return dataTypeSelector.isBigEndianEncoded()
                        ? getAsciiParser(true)
                        : getAsciiParser(false);

            case UNKNOWN:
            default:
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
                        return new BigDecimal(ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(intBitsArray, 0), ""));
                    } else {
                        throw new ModbusException("ParserFactory, BCDParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, BCDParser, failed to parse the value: " + e.getMessage());
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

    private byte[] getByteArrayFromValue(int[] values, boolean bigEndianEncoding) {
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
}
