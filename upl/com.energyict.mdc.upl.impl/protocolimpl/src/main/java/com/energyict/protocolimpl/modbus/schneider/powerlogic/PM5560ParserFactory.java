package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.generic.ParserFactory;
import com.energyict.protocolimpl.modbus.generic.common.DataType;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.common.PM5560DataTypeSelector;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author sva
 * @since 18/03/2015 - 11:27
 */
public class PM5560ParserFactory extends ParserFactory {

    @Override
    public Parser get(String key) throws IOException {
        DataTypeSelector dataTypeSelector = PM5560DataTypeSelector.getDataTypeSelector(Integer.parseInt(key));
        DataType dataType = dataTypeSelector.getDataType();
        if (dataType.equals(PM5560DataTypeSelector.POWER_FACTOR_DATA_TYPE)) {
            return dataTypeSelector.isBigEndianEncoded()
                    ? getPowerFactorParser(true)
                    : getPowerFactorParser(false);
//        } else if (dataType.equals(PM5560DataTypeSelector.DATE_TIME_DATA_TYPE)) {
//            return dataTypeSelector.isBigEndianEncoded()
//                    ? getDateTimeParser(true)
//                    : getDateTimeParser(false);
        } else {
            return super.get(key);
        }
    }

    private Parser getPowerFactorParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    BigDecimal rawValue;
                    if (values.length == 2) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        rawValue = new BigDecimal(Float.toString(Float.intBitsToFloat(bigInteger.intValue())));
                    } else if (values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        rawValue = new BigDecimal(Double.toString(Double.longBitsToDouble(bigInteger.longValue())));
                    } else {
                        throw new ModbusException("ParserFactory, PowerFactorParser, received data has invalid length (" + values.length + ")");
                    }

                    if (rawValue.floatValue() > 1) {
                        return BigDecimal.valueOf(2).subtract(rawValue);
                    } else if (rawValue.floatValue() < -1) {
                        return BigDecimal.valueOf(-2).subtract(rawValue);
                    } else {
                        return rawValue;
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, PowerFactorParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getDateTimeParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 4) {
                        return new DateTime().parseDateTime(
                                bigEndianEncoding
                                        ? values
                                        : convertLittleToBigEndian(values)
                        );
                    } else {
                        throw new ModbusException("ParserFactory, PowerFactorParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (NumberFormatException e) {
                    throw new ModbusException("ParserFactory, PowerFactorParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }
}