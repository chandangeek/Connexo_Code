package com.energyict.protocolimpl.modbus.multilin.epm2200;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {

    /**
     * Creates a new instance of RegisterFactory
     */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    protected void init() {
        // options
        setZeroBased(true); // this means that reg2read = reg-1

        // registers
        // Fixed Data Section
        // + Identification block
        getRegisters().add(new HoldingRegister(1, 8, "MeterName").setParser("ASCII"));
        getRegisters().add(new HoldingRegister(9, 8, ObisCode.fromString("0.0.96.1.0.255"), "MeterSerial").setParser("ASCII"));
        getRegisters().add(new HoldingRegister(17, 1, "MeterType").setParser("UINT16"));
        getRegisters().add(new HoldingRegister(18, 2, "FirmwareVersion").setParser("ASCII"));

        // Meter Data Section
        // + Primary readings block
        getRegisters().add(new HoldingRegister(1000, 2, ObisCode.fromString("1.1.132.7.0.255")).setParser("FLOAT"));    // RMS Phase-to-Neutral voltage V1N
        getRegisters().add(new HoldingRegister(1002, 2, ObisCode.fromString("1.1.152.7.0.255")).setParser("FLOAT"));    // RMS Phase-to-Neutral voltage V2N
        getRegisters().add(new HoldingRegister(1004, 2, ObisCode.fromString("1.1.172.7.0.255")).setParser("FLOAT"));    // RMS Phase-to-Neutral voltage V3N
        getRegisters().add(new HoldingRegister(1006, 2, ObisCode.fromString("1.1.32.7.0.255")).setParser("FLOAT"));     // RMS Phase-to-Phase voltage V12
        getRegisters().add(new HoldingRegister(1008, 2, ObisCode.fromString("1.1.52.7.0.255")).setParser("FLOAT"));     // RMS Phase-to-Phase voltage V23
        getRegisters().add(new HoldingRegister(1010, 2, ObisCode.fromString("1.1.72.7.0.255")).setParser("FLOAT"));     // RMS Phase-to-Phase voltage V31
        getRegisters().add(new HoldingRegister(1012, 2, ObisCode.fromString("1.1.31.7.0.255")).setParser("FLOAT"));     // RMS current on phase 1:L1
        getRegisters().add(new HoldingRegister(1014, 2, ObisCode.fromString("1.1.51.7.0.255")).setParser("FLOAT"));     // RMS current on phase 2:L2
        getRegisters().add(new HoldingRegister(1016, 2, ObisCode.fromString("1.1.71.7.0.255")).setParser("FLOAT"));     // RMS current on phase 3:L3
        getRegisters().add(new HoldingRegister(1018, 2, ObisCode.fromString("1.1.1.7.0.255")).setParser("FLOAT"));      // Active power Total
        getRegisters().add(new HoldingRegister(1020, 2, ObisCode.fromString("1.1.3.7.0.255")).setParser("FLOAT"));      // Reactive power Total
        getRegisters().add(new HoldingRegister(1022, 2, ObisCode.fromString("1.1.9.7.0.255")).setParser("FLOAT"));      // Apparent power Total
        getRegisters().add(new HoldingRegister(1024, 2, ObisCode.fromString("1.1.13.7.0.255")).setParser("FLOAT"));     // Total power factor
        getRegisters().add(new HoldingRegister(1026, 2, ObisCode.fromString("1.1.14.7.0.255")).setParser("FLOAT"));     // Current frequency
        getRegisters().add(new HoldingRegister(1028, 2, ObisCode.fromString("1.1.91.7.0.255")).setParser("FLOAT"));     // Neutral current

        // + Primary energy block
        getRegisters().add(new HoldingRegister(1100, 2, ObisCode.fromString("1.1.1.8.0.255")).setParser("SINT32"));     // Active energy +
        getRegisters().add(new HoldingRegister(1102, 2, ObisCode.fromString("1.1.2.8.0.255")).setParser("SINT32"));     // Active energy -
        getRegisters().add(new HoldingRegister(1104, 2, ObisCode.fromString("1.1.1.9.0.255")).setParser("SINT32"));     // Active energy, Net
        getRegisters().add(new HoldingRegister(1106, 2, ObisCode.fromString("1.1.1.10.0.255")).setParser("SINT32"));    // Active energy, Total
        getRegisters().add(new HoldingRegister(1108, 2, ObisCode.fromString("1.1.3.8.0.255")).setParser("SINT32"));     // Reactive energy +
        getRegisters().add(new HoldingRegister(1110, 2, ObisCode.fromString("1.1.4.8.0.255")).setParser("SINT32"));     // Reactive energy -
        getRegisters().add(new HoldingRegister(1112, 2, ObisCode.fromString("1.1.3.9.0.255")).setParser("SINT32"));     // Reactive energy, Net
        getRegisters().add(new HoldingRegister(1114, 2, ObisCode.fromString("1.1.3.10.0.255")).setParser("SINT32"));     // Reactive energy, Total
        getRegisters().add(new HoldingRegister(1116, 2, ObisCode.fromString("1.1.9.8.0.255")).setParser("SINT32"));     // Apparent energy, Total

        // + Primary demand block
        getRegisters().add(new HoldingRegister(2000, 2, ObisCode.fromString("1.1.31.4.0.255")).setParser("FLOAT"));     // Average RMS current on phase 1:L1
        getRegisters().add(new HoldingRegister(2002, 2, ObisCode.fromString("1.1.51.4.0.255")).setParser("FLOAT"));     // Average RMS current on phase 2:L2
        getRegisters().add(new HoldingRegister(2004, 2, ObisCode.fromString("1.1.71.4.0.255")).setParser("FLOAT"));     // Average RMS current on phase 3:L3
        getRegisters().add(new HoldingRegister(2006, 2, ObisCode.fromString("1.1.1.4.0.255")).setParser("FLOAT"));      // Average Active power + Total
        getRegisters().add(new HoldingRegister(2010, 2, ObisCode.fromString("1.1.2.4.0.255")).setParser("FLOAT"));      // Average Active power - Total
        getRegisters().add(new HoldingRegister(2008, 2, ObisCode.fromString("1.1.3.4.0.255")).setParser("FLOAT"));       // Average Reactive power + Total
        getRegisters().add(new HoldingRegister(2012, 2, ObisCode.fromString("1.1.4.4.0.255")).setParser("FLOAT"));       // Average Reactive power - Total
        getRegisters().add(new HoldingRegister(2014, 2, ObisCode.fromString("1.1.9.4.0.255")).setParser("FLOAT"));      // Average Apparent power Total
        getRegisters().add(new HoldingRegister(2016, 2, ObisCode.fromString("1.1.13.4.0.255")).setParser("FLOAT"));     // Average Total power factor +
        getRegisters().add(new HoldingRegister(2018, 2, ObisCode.fromString("1.1.84.4.0.255")).setParser("FLOAT"));     // Average Total power factor -

        // + Primary minimum block
        getRegisters().add(new HoldingRegister(3000, 2, ObisCode.fromString("1.1.132.3.0.255")).setParser("FLOAT"));    // Minimum RMS Phase-to-Neutral voltage V1N
        getRegisters().add(new HoldingRegister(3002, 2, ObisCode.fromString("1.1.152.3.0.255")).setParser("FLOAT"));    // Minimum RMS Phase-to-Neutral voltage V2N
        getRegisters().add(new HoldingRegister(3004, 2, ObisCode.fromString("1.1.172.3.0.255")).setParser("FLOAT"));    // Minimum RMS Phase-to-Neutral voltage V3N
        getRegisters().add(new HoldingRegister(3006, 2, ObisCode.fromString("1.1.32.3.0.255")).setParser("FLOAT"));     // Minimum RMS Phase-to-Phase voltage V12
        getRegisters().add(new HoldingRegister(3008, 2, ObisCode.fromString("1.1.52.3.0.255")).setParser("FLOAT"));     // Minimum RMS Phase-to-Phase voltage V23
        getRegisters().add(new HoldingRegister(3010, 2, ObisCode.fromString("1.1.72.3.0.255")).setParser("FLOAT"));     // Minimum RMS Phase-to-Phase voltage V31
        getRegisters().add(new HoldingRegister(3012, 2, ObisCode.fromString("1.1.31.3.0.255")).setParser("FLOAT"));     // Minimum RMS current on phase 1:L1
        getRegisters().add(new HoldingRegister(3014, 2, ObisCode.fromString("1.1.51.3.0.255")).setParser("FLOAT"));     // Minimum RMS current on phase 2:L2
        getRegisters().add(new HoldingRegister(3016, 2, ObisCode.fromString("1.1.51.3.0.255")).setParser("FLOAT"));     // Minimum RMS current on phase 3:L3
        getRegisters().add(new HoldingRegister(3018, 2, ObisCode.fromString("1.1.1.3.0.255")).setParser("FLOAT"));      // Minimum Active power + Total
        getRegisters().add(new HoldingRegister(3022, 2, ObisCode.fromString("1.1.2.3.0.255")).setParser("FLOAT"));      // Minimum Active power - Total
        getRegisters().add(new HoldingRegister(3020, 2, ObisCode.fromString("1.1.3.3.0.255")).setParser("FLOAT"));      // Minimum Reactive power + Total
        getRegisters().add(new HoldingRegister(3024, 2, ObisCode.fromString("1.1.4.3.0.255")).setParser("FLOAT"));      // Minimum Reactive power - Total
        getRegisters().add(new HoldingRegister(3026, 2, ObisCode.fromString("1.1.9.3.0.255")).setParser("FLOAT"));      // Minimum Apparent power Total
        getRegisters().add(new HoldingRegister(3028, 2, ObisCode.fromString("1.1.13.3.0.255")).setParser("FLOAT"));     // Minimum Total power factor +
        getRegisters().add(new HoldingRegister(3030, 2, ObisCode.fromString("1.1.84.3.0.255")).setParser("FLOAT"));     // Minimum Total power factor -
        getRegisters().add(new HoldingRegister(3032, 2, ObisCode.fromString("1.1.14.3.0.255")).setParser("FLOAT"));     // Minimum frequency

        // + Primary maximum block
        getRegisters().add(new HoldingRegister(3100, 2, ObisCode.fromString("1.1.132.6.0.255")).setParser("FLOAT"));    // Maximum RMS Phase-to-Neutral voltage V1N
        getRegisters().add(new HoldingRegister(3102, 2, ObisCode.fromString("1.1.152.6.0.255")).setParser("FLOAT"));    // Maximum RMS Phase-to-Neutral voltage V2N
        getRegisters().add(new HoldingRegister(3104, 2, ObisCode.fromString("1.1.172.6.0.255")).setParser("FLOAT"));    // Maximum RMS Phase-to-Neutral voltage V3N
        getRegisters().add(new HoldingRegister(3106, 2, ObisCode.fromString("1.1.32.6.0.255")).setParser("FLOAT"));     // Maximum RMS Phase-to-Phase voltage V12
        getRegisters().add(new HoldingRegister(3108, 2, ObisCode.fromString("1.1.52.6.0.255")).setParser("FLOAT"));     // Maximum RMS Phase-to-Phase voltage V23
        getRegisters().add(new HoldingRegister(3110, 2, ObisCode.fromString("1.1.72.6.0.255")).setParser("FLOAT"));     // Maximum RMS Phase-to-Phase voltage V31
        getRegisters().add(new HoldingRegister(3112, 2, ObisCode.fromString("1.1.31.6.0.255")).setParser("FLOAT"));     // Maximum RMS current on phase 1:L1
        getRegisters().add(new HoldingRegister(3114, 2, ObisCode.fromString("1.1.51.6.0.255")).setParser("FLOAT"));     // Maximum RMS current on phase 2:L2
        getRegisters().add(new HoldingRegister(3116, 2, ObisCode.fromString("1.1.51.6.0.255")).setParser("FLOAT"));     // Maximum RMS current on phase 3:L3
        getRegisters().add(new HoldingRegister(3118, 2, ObisCode.fromString("1.1.1.6.0.255")).setParser("FLOAT"));      // Maximum Active power + Total
        getRegisters().add(new HoldingRegister(3122, 2, ObisCode.fromString("1.1.2.6.0.255")).setParser("FLOAT"));      // Maximum Active power - Total
        getRegisters().add(new HoldingRegister(3120, 2, ObisCode.fromString("1.1.3.6.0.255")).setParser("FLOAT"));      // Maximum Reactive power + Total
        getRegisters().add(new HoldingRegister(3124, 2, ObisCode.fromString("1.1.4.6.0.255")).setParser("FLOAT"));      // Maximum Reactive power - Total
        getRegisters().add(new HoldingRegister(3126, 2, ObisCode.fromString("1.1.9.6.0.255")).setParser("FLOAT"));      // Maximum Apparent power Total
        getRegisters().add(new HoldingRegister(3128, 2, ObisCode.fromString("1.1.13.6.0.255")).setParser("FLOAT"));     // Maximum Total power factor +
        getRegisters().add(new HoldingRegister(3130, 2, ObisCode.fromString("1.1.84.6.0.255")).setParser("FLOAT"));     // Maximum Total power factor -
        getRegisters().add(new HoldingRegister(3132, 2, ObisCode.fromString("1.1.14.6.0.255")).setParser("FLOAT"));     // Maximum frequency

        // + Phase Angle Block
        getRegisters().add(new HoldingRegister(4100, 1, ObisCode.fromString("1.0.81.7.44.255")).setParser("SINT16"));   // RMS current, Phase angel L1
        getRegisters().add(new HoldingRegister(4101, 1, ObisCode.fromString("1.0.81.7.55.255")).setParser("SINT16"));   // RMS current, Phase angle L2
        getRegisters().add(new HoldingRegister(4102, 1, ObisCode.fromString("1.0.81.7.66.255")).setParser("SINT16"));   // RMS current, Phase angle L3
        getRegisters().add(new HoldingRegister(4103, 1, ObisCode.fromString("1.0.81.7.10.255")).setParser("SINT16"));   // RMS voltage, Phase angle U1-U2
        getRegisters().add(new HoldingRegister(4104, 1, ObisCode.fromString("1.0.81.7.21.255")).setParser("SINT16"));   // RMS voltage, Phase angle U2-U3
        getRegisters().add(new HoldingRegister(4105, 1, ObisCode.fromString("1.0.81.7.02.255")).setParser("SINT16"));   // RMS voltage, Phase angle U3-U1

        // + Basic Setup Block
        getRegisters().add(new HoldingRegister(40016, 1, ObisCode.fromString("1.1.0.4.2.255")).setParser("UINT16"));   // CT numerator
        getRegisters().add(new HoldingRegister(40017, 1, ObisCode.fromString("1.1.0.4.8.255")).setParser("UINT16"));   // CT multiplier
        getRegisters().add(new HoldingRegister(40018, 1, ObisCode.fromString("1.1.0.4.5.255")).setParser("UINT16"));   // CT denominator
        getRegisters().add(new HoldingRegister(40019, 1, ObisCode.fromString("1.1.0.4.3.255")).setParser("UINT16"));   // PT numerator
        getRegisters().add(new HoldingRegister(40020, 1, ObisCode.fromString("1.1.0.4.9.255")).setParser("UINT16"));   // PT multiplier
        getRegisters().add(new HoldingRegister(40021, 1, ObisCode.fromString("1.1.0.4.6.255")).setParser("UINT16"));   // PT denominator
    }

    protected void initParsers() {
        // ASCII character parser
         getParserFactory().addParser("ASCII", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                String asciiString = "";
                for (int i = 0; i < values.length; i++) {
                   asciiString += (char) (values[i]>>8 &0xFF);
                   asciiString += (char) (values[i] &0xFF);
                }
                return asciiString.trim();
            }
        });

        // 16-bit unsigned integer parser
        getParserFactory().addParser("UINT16", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return new BigDecimal(values[0]);   // (in fact we thread it as being an signed 32-bit integer - but that makes no difference)
            }
        });

        // floating point value parser
        getParserFactory().addParser("FLOAT", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                if (values.length == 2) {
                    byte[] intBitsArray = new byte[]{(byte) (values[0] >> 8 &0xFF), (byte) (values[0] & 0xFF),
                            (byte) (values[1] >> 8 & 0xFF), (byte) (values[1] & 0xFF)};
                    try {
                        int intBits = ProtocolUtils.getInt(intBitsArray, 0, 4);
                        float val = Float.intBitsToFloat(intBits);
                        return new BigDecimal(String.valueOf(val));
                    } catch (IOException e) {
                    }
                }
                return new BigDecimal(0);
            }
        });

        // 16-bit signed integer parser
        getParserFactory().addParser("SINT16", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                byte[] shortBitsArray = new byte[]{(byte) (values[0] >> 8 & 0xFF), (byte) (values[0] & 0xFF)};
                short s = (short) ((shortBitsArray[0] << 8) | shortBitsArray[1]);
                return BigDecimal.valueOf(s).multiply(new BigDecimal("0.1"));
            }
        });

        // 32-bit signed integer parser
        getParserFactory().addParser("SINT32", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                 if (values.length == 2) {
                    byte[] intBitsArray = new byte[]{(byte) (values[0] >> 8 &0xFF), (byte) (values[0] & 0xFF),
                            (byte) (values[1] >> 8 & 0xFF), (byte) (values[1] & 0xFF)};
                    try {
                        int val = ProtocolUtils.getInt(intBitsArray, 0, 4);
                        return new BigDecimal(String.valueOf(val));
                    } catch (IOException e) {
                    }
                }
                return new BigDecimal(0);
            }
        });
    }
}