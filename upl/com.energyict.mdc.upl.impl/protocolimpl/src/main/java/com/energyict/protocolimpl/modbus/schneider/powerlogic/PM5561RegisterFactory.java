package com.energyict.protocolimpl.modbus.schneider.powerlogic;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class PM5561RegisterFactory extends AbstractRegisterFactory{
    protected static final String CurrentDateTime = "CurrentDateTime";
    public final static String profileInterval	= "profileInterval";
    public final static String channelInfos	= "channelInfos";
    public static final String SERIAL_NUMBER = "SerialNo";
    private static final String Ascii16Parser = "Ascii16Parser";
    private static final String AsciiParser = "AsciiParser";
    private static final String UnsignedValueParser = "UnsignedValueParser";
    private static final String UnsignedValueCheckNotAvailableParser = "UnsignedValueCheckNotAvailableParser";
    private static final String SignedValueParser = "SignedValueParser";
    private static final String SignedValueCheckNotAvailableParser = "SignedValueCheckNotAvailableParser";
    public static final String PARSER_UINT32	= "UINT32_Parser"; 		// 4 bytes (long)
    public static final String PARSER_UINT16	= "UINT16_Parser";

    private static final boolean BIG_ENDIAN_ENCODING = true;
    public static final String VOLTAGE = "voltage";
    private static final String DATETIME = "DATETIME";
    private static final int CENTI_SCALE = -2;
    private static final int MILLI_SCALE = -3;
    private static final int KILO_SCALE = 3;
    private static final int DECA_SCALE = 1;
    private static final int MEGA_SCALE = 6;

    public static final String LOAD_PROFILE_STATUS = "Load Profile Status";
    public static final String LOAD_PROFILE_RECORD_ITEM1 = "Load Profile Configuration";
    public static final String LOAD_PROFILE_FIRST_RECORD = "Load Profile Configuration - First Record Sequence Number";
    public static final String LOAD_PROFILE_LAST_RECORD = "Load Profile Configuration - Last Record Sequence Number";
    public static final String LOAD_PROFILE_NUMBER_OF_RECORDS = "Number of records in file";
    public static final String CommandParameter1 = "CommandParameter1";

    public PM5561RegisterFactory(Modbus modBus) {
        super(modBus);
    }


    @Override
    protected void init() {
        setZeroBased(true);

        //Product identification
        getRegisters().add(new HoldingRegister(0x1E, 20, ObisCode.fromString("1.0.96.0.0.255"), "Meter Name").setParser(AsciiParser));
        getRegisters().add(new HoldingRegister(0x32, 20, ObisCode.fromString("1.0.96.1.0.255"), "Meter Model").setParser(AsciiParser));
        getRegisters().add(new HoldingRegister(0x46, 20, ObisCode.fromString("1.0.96.2.0.255"), "Manufacturer").setParser(AsciiParser));
        getRegisters().add(new HoldingRegister(0x5A, 1, ObisCode.fromString("1.0.96.3.0.255"), "Product ID Number").setParser(AsciiParser));
        getRegisters().add(new HoldingRegister(0x82, 2, ObisCode.fromString("1.0.96.4.0.255"), SERIAL_NUMBER).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x84, 2, ObisCode.fromString("1.0.96.5.0.255"), "Date of Manufacture").setParser(DATETIME));
        getRegisters().add(new HoldingRegister(0x88, 2, ObisCode.fromString("1.0.96.6.0.255"), "Hardware Revision").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x665, 2, ObisCode.fromString("1.0.96.7.0.255"), "Present Firmware Version (DLF Format) X.Y.T").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x66A, 2, ObisCode.fromString("1.0.96.8.0.255"), "Previous Firmware Version (DLF Format) X.Y.T").setParser(UnsignedValueParser));

        //Meteorology affected by current and voltage transformers
        getRegisters().add(new HoldingRegister(0xBCC, 2, ObisCode.fromString("1.0.128.7.0.255"), "Voltage A-B").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBCE, 2, ObisCode.fromString("1.0.129.7.0.255"), "Voltage B-C").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBD0, 2, ObisCode.fromString("1.0.130.7.0.255"), "Voltage C-A").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBD2, 2, ObisCode.fromString("1.0.131.7.0.255"), "Voltage L-L Avg").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBE6, 2, ObisCode.fromString("1.0.52.7.0.255"), "Voltage Unbalance A-N").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBEA, 2, ObisCode.fromString("1.0.72.7.0.255"), "Voltage Unbalance C-N").setParser(VOLTAGE));
        getRegisters().add(new HoldingRegister(0xBB8, 2, ObisCode.fromString("1.0.31.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current A").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBBA, 2, ObisCode.fromString("1.0.51.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current B").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBBC, 2, ObisCode.fromString("1.0.71.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current C").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBBE, 2, ObisCode.fromString("1.0.91.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Neutral N").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBF4, 2, ObisCode.fromString("1.0.1.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power Total").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBFC, 2, ObisCode.fromString("1.0.3.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power Total").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC04, 2, ObisCode.fromString("1.0.9.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, DECA_SCALE), "Apparent Power Total").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC0C, 2, ObisCode.fromString("1.0.13.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor Total").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBEE, 2, ObisCode.fromString("1.0.21.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power A").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBF0, 2, ObisCode.fromString("1.0.41.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power B").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBF2, 2, ObisCode.fromString("1.0.61.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power C").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBF6, 2, ObisCode.fromString("1.0.23.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power A").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBF8, 2, ObisCode.fromString("1.0.43.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power B").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBFA, 2, ObisCode.fromString("1.0.63.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power C").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xBFE, 2, ObisCode.fromString("1.0.29.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Power A").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC00, 2, ObisCode.fromString("1.0.49.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Power B").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC02, 2, ObisCode.fromString("1.0.69.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Power C").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC06, 2, ObisCode.fromString("1.0.33.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor A").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC08, 2, ObisCode.fromString("1.0.53.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor B").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC0A, 2, ObisCode.fromString("1.0.73.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor C").setParser(SignedValueCheckNotAvailableParser));

        //Energy values
        getRegisters().add(new HoldingRegister(0xC84, 4, ObisCode.fromString("1.0.1.8.0.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Active Energy Delivered (Into Load)").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC88, 4, ObisCode.fromString("1.0.2.8.0.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Active Energy Received (Out of Load)").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC94, 4, ObisCode.fromString("1.0.3.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Reactive Energy Delivered").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC98, 4, ObisCode.fromString("1.0.4.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Reactive Energy Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCA4, 4, ObisCode.fromString("1.0.9.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Energy Delivered").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCA8, 4, ObisCode.fromString("1.0.10.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Energy Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC9C, 4, ObisCode.fromString("1.0.3.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Reactive Energy Delivered + Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCB0, 4, ObisCode.fromString("1.0.2.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Energy Delivered - Received").setParser(SignedValueParser));

        getRegisters().add(new HoldingRegister(0xC8C, 4, ObisCode.fromString("1.0.1.8.1.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Active Energy Delivered + Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC9C, 4, ObisCode.fromString("1.0.3.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Reactive Energy Delivered + Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xC90, 4, ObisCode.fromString("1.0.1.8.2.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Active Energy Delivered- Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCAC, 4, ObisCode.fromString("1.0.9.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Energy Delivered + Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCA0, 4, ObisCode.fromString("1.0.3.8.2.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Reactive Energy Delivered - Received").setParser(SignedValueParser));
        getRegisters().add(new HoldingRegister(0xCA0, 4, ObisCode.fromString("1.0.9.8.2.255"), Unit.get(BaseUnit.VOLTAMPEREHOUR, KILO_SCALE), "Apparent Energy Delivered - Received").setParser(SignedValueParser));

//        getRegisters().add(new HoldingRegister(0xC68A, 2, ObisCode.fromString("1.0.131.5.0.255"), Unit.get(BaseUnit.SECOND), "Last date for Record average P/Q/S in second since 01/01/2000").setParser(UnsignedValueCheckNotAvailableParser));
//        getRegisters().add(new HoldingRegister(0xC68C, 1, ObisCode.fromString("1.0.1.5.0.255"), Unit.get(BaseUnit.WATT), "Last average (P+) (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));

        //CurrentDateTime
        getRegisters().add(new HoldingRegister(0x72C, 6, CurrentDateTime));
        getRegisters().add(new HoldingRegister(0x138A, 1, CommandParameter1));

        //Load Profiles
        getRegisters().add(new HoldingRegister(0x856, 1, ObisCode.fromString("0.1.128.0.0.255"), "Energy Channel").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x4A38, 1, ObisCode.fromString("170.3.74.56.3.255"), "Logging Status").setParser(UnsignedValueParser));

        // LoadProfile related registers
        getRegisters().add(new HoldingRegister(0x4A38, 1, ObisCode.fromString("170.3.74.60.3.255"), LOAD_PROFILE_STATUS).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x4A49, 1, ObisCode.fromString("170.3.74.73.3.255"), LOAD_PROFILE_RECORD_ITEM1).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x4A3E, 1, ObisCode.fromString("170.3.74.62.3.255"), LOAD_PROFILE_FIRST_RECORD).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x4A3F, 1, ObisCode.fromString("170.3.74.63.3.255"),LOAD_PROFILE_LAST_RECORD).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x4A3D, 1, ObisCode.fromString("170.3.74.61.3.255"),LOAD_PROFILE_NUMBER_OF_RECORDS).setParser(UnsignedValueParser));

    }


    @Override
    protected void initParsers() {
        getParserFactory().addParser(UnsignedValueParser, getUnsignedValueParser(true));
        getParserFactory().addParser(UnsignedValueCheckNotAvailableParser, getUnsignedValueCheckNotAvailableParser(true));
        getParserFactory().addParser(SignedValueParser, getSignedValueParser(true));
        getParserFactory().addParser(SignedValueCheckNotAvailableParser, getSignedValueCheckNotAvailableParser(true));
        getParserFactory().addParser(AsciiParser, getAsciiParser(BIG_ENDIAN_ENCODING, false));
        getParserFactory().addParser(Ascii16Parser, getAsciiParser(BIG_ENDIAN_ENCODING, true));
        getParserFactory().addParser(PARSER_UINT32, new UINT32Parser());
        getParserFactory().addParser(PARSER_UINT16, new UINT16Parser());
        getParserFactory().addParser(VOLTAGE,new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                return new BigDecimal(val);
            }
        });

        getParserFactory().addParser(DATETIME, getDateTimeParser(true));
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
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, UnsignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getUnsignedValueCheckNotAvailableParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 || values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                        if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("FFFF", 16)) ||
                                ((values.length == 2) && bigInteger.longValue() == Long.parseLong("FFFFFFFF", 16))) {
                            bigInteger = new BigInteger("0");   // The value is not available
                        }
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, UnsignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, UnsignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getSignedValueParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 | values.length == 2 | values.length == 4) {           // Signed register
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(bigInteger.intValue()/1000);
                    } else {
                        throw new ModbusException("ParserFactory, SignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, SignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getSignedValueCheckNotAvailableParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 | values.length == 2 | values.length == 4) {           // Signed register
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("7FFF", 16)) ||
                                ((values.length == 2) && bigInteger.longValue() == Long.parseLong("7FFFFFFF", 16))) {
                            bigInteger = new BigInteger("0");   // The value is not available
                        }
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, SignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, SignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getAsciiParser(final boolean bigEndianEncoding, final boolean isAscii16) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("FFFF", 16)) ||
                        ((values.length == 2) && bigInteger.longValue() == Long.parseLong("FFFFFFFF", 16))) {
                    return "";   // The value is not available
                }

                String asciiString = "";
                if (bigEndianEncoding) {
                    for (int i = 0; i < values.length; i++) {
                        if (!isAscii16) {
                            asciiString += (char) (values[i] >> 8 & 0xFF);
                        }
                        asciiString += (char) (values[i] & 0xFF);
                    }
                } else {
                    for (int i = (values.length - 1); i >= 0; i--) {
                        if (!isAscii16) {
                            asciiString += (char) (values[i] >> 8 & 0xFF);
                        }
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

    public class UINT32Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            Integer returnValue =
                    new Integer(
                            ((values[0] & 0x0000FF00) >> 8) +
                                    ((values[0] & 0x000000FF) << 8) +
                                    ((values[1] & 0x0000FF00) << 8) +
                                    ((values[1] & 0x000000FF) << 24)
                    );
            return returnValue;
        }
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


    public class UINT16Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            Integer returnValue =
                    new Integer(
                            ((values[0] & 0x0000FF00) >> 8) +
                                    ((values[0] & 0x000000FF) << 8)
                    );
            return returnValue;
        }
    }

    protected int[] convertLittleToBigEndian(int[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[values.length - 1 - i];
        }
        return result;
    }
}
