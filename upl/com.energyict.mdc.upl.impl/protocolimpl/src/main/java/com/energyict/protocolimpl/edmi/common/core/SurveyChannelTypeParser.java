package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Range;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.util.Pair;

import java.math.BigDecimal;

/**
 * @author jme
 *         Changes:
 *         GNA |26022009| Added the units for instantaneous values
 */
public class SurveyChannelTypeParser {

    private static final int ALL = 0x00;
    private static final int POSITIVE = 0x01;
    private static final int NEGATIVE = 0x02;
    private static final int POSITIVE_Q1 = 0x03;
    private static final int NEGATIVE_Q2 = 0x04;
    private static final int NEGATIVE_Q3 = 0x05;
    private static final int POSITIVE_Q4 = 0x06;
    private static final Range ACTIVE_POWER_RANGE = new Range(0, 3);
    private static final Range REACTIVE_POWER_RANGE = new Range(4, 7);
    private static final Range APPARENT_POWER_RANGE = new Range(8, 11);

    private int decimalPointPosition;
    private int ScalingFactor;
    private int instantType;
    private Unit unit;
    private boolean channelEnabled = true;
    private boolean instant = false;
    private ObisCode obisCode;

    private final static int VOLTS = 0;
    private final static int AMPS = 1;
    private final static int POWER = 2;
    private final static int ANGLE = 3;
    private final static int FREQ = 4;

    public SurveyChannelTypeParser(int channelDef) throws ProtocolException {
        int regValue = channelDef & 0x000F;                         // Bits 0-7 give the source definition
        int regFunction = (channelDef & 0x00F0) >> 4;

        Type type = Type.fromTypeValue(((channelDef & 0x8000) >> 13) + ((channelDef & 0x0300) >> 8));    // Bits 8-9 give the acc/min/max/inst setting bottom 2 bits
        // Bit 15 gives the acc/min/max/inst setting top bit
        int scalingCode = (channelDef & 0x0C00) >> 10;              // Bits 10-11 give the scaling code
        this.decimalPointPosition = (channelDef & 0x7000) >> 12;    // Bits 12-14 give the position of the decimal point

        boolean isEnergy = false;
        boolean isPulse = false;
        boolean isInstantaneous = false;
        boolean isTesting = false;
        if ((channelDef & 0x00FF) == 0x00FF) {
            channelEnabled = false;
        }

        switch (regFunction) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
                isEnergy = true; // Energy
                break;
            case 0x07:
                isPulse = true; // Pulsing input
                break;
            case 0x08:
            case 0x09:
                isInstantaneous = true; // 5 cycle readings
                break;
            case 0x0A:
            case 0x0B:
                isInstantaneous = true; // 5 cycle readings, frequency compensated
                break;
            case 0x0C:
            case 0x0D:
                isPulse = true; // Extra pulse input channels
                break;
            case 0x0F:
                isTesting = true; // Reserved for test functions
                break;
            default:
                throw new ProtocolException("Load survey channel definition contains invalid/unsupported register function '" + regFunction + "'");
        }

        if (isEnergy) {
            Phase phase = parseEnergyChannel(regFunction, regValue, scalingCode);
            int baseObisC = parseEnergyReadingToObisC(regFunction, regValue);
            constructChannelObisCode(baseObisC, phase, type);
        } else if (isInstantaneous) {
            Pair<Integer, Integer> pair = parseInstantaneousChannel(regValue, regFunction);   // scalingCode not used here, as for instantaneous channels the scaling factor should be read out separate (from D808-C)
            constructChannelObisCode(pair.getFirst(), Phase.NOT_APPLICABLE, type, pair.getLast());
        } else if (isTesting) {
            Pair<Integer, Integer> pair = parseTestChannel(regFunction, regValue, scalingCode);
            constructChannelObisCode(pair.getFirst(), Phase.NOT_APPLICABLE, type, pair.getLast());
        } else if (isPulse) {
            Pair<Integer, Integer> pair = parsePulseChannel(regValue, scalingCode);
            constructChannelObisCode(pair.getFirst(), Phase.NOT_APPLICABLE, type, pair.getLast());
        }
    }

    private Phase parseEnergyChannel(int regFunction, int regValue, int scaling) throws ProtocolException {
        this.ScalingFactor = 1;
        switch (regValue) {
            case 0x00:
                this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
                return Phase.PHASE_A;
            case 0x01:
                this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
                return Phase.PHASE_B;
            case 0x02:
                this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
                return Phase.PHASE_C;
            case 0x03:
                this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
                return Phase.TOTAL;
            case 0x04:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
                return Phase.PHASE_A;
            case 0x05:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
                return Phase.PHASE_B;
            case 0x06:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
                return Phase.PHASE_C;
            case 0x07:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
                return Phase.TOTAL;
            case 0x08:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
                return Phase.PHASE_A;
            case 0x09:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
                return Phase.PHASE_B;
            case 0x0A:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
                return Phase.PHASE_C;
            case 0x0B:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
                return Phase.TOTAL;
            default:
                throw new ProtocolException("Load survey channel definition contains invalid/unsupported energy register (function '" + regFunction + "'" + ", value '" + regValue + "')");
        }
    }

    private Pair<Integer, Integer> parseInstantaneousChannel(int regValue, int regFunction) throws ProtocolException {
        this.ScalingFactor = 1;
        this.instant = true;
        regValue = regValue + (regFunction << 4) & 0x1F;

        Integer dField = null;
        switch (regValue) {
            case 0x00:
            case 0x01:
            case 0x02:
                this.unit = Unit.get(BaseUnit.AMPERE); // Channel value base unit is current ABC
                this.instantType = AMPS;
                return new Pair<>(11 + mapRegValueToPhase(regValue, 0x00).getOffset(), 0);
            case 0x03:
            case 0x04:
            case 0x05:
                this.unit = Unit.get(BaseUnit.VOLT); // Channel value base unit is voltage ABC
                this.instantType = VOLTS;
                return new Pair<>(12 + mapRegValueToPhase(regValue, 0x03).getOffset(), 0);
            case 0x06:
            case 0x07:
            case 0x08:
                this.unit = Unit.get(BaseUnit.WATT); // Channel value base unit is watt ABC
                this.instantType = POWER;
                return new Pair<>(1 + mapRegValueToPhase(regValue, 0x06).getOffset(), 0);
            case 0x09:
            case 0x0A:
            case 0x0B:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); // Channel value base unit is var ABC
                this.instantType = POWER;
                return new Pair<>(3 + mapRegValueToPhase(regValue, 0x09).getOffset(), 0);
            case 0x0C:
            case 0x0D:
            case 0x0E:
                this.unit = Unit.get(BaseUnit.VOLTAMPERE); // Channel value base unit is VA ABC
                this.instantType = POWER;
                return new Pair<>(9 + mapRegValueToPhase(regValue, 0x0C).getOffset(), 0);
            case 0x0F:
                this.unit = Unit.get(BaseUnit.HERTZ); // Channel value base unit is frequency
                this.instantType = FREQ;
                return new Pair<>(14, 0);
            case 0x10:
                this.unit = Unit.get(BaseUnit.AMPERE); // Channel value base unit is current ABC average
                this.instantType = AMPS;
                return new Pair<>(11, 0);
            case 0x11:
                this.unit = Unit.get(BaseUnit.VOLT); // Channel value base unit is voltage ABC average
                this.instantType = VOLTS;
                return new Pair<>(12, 0);
            case 0x12:
                this.unit = Unit.get(BaseUnit.WATT); // Channel value base unit is watt sum
                this.instantType = POWER;
                return new Pair<>(1, 0);
            case 0x13:
                this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); // Channel value base unit is var sum
                this.instantType = POWER;
                return new Pair<>(3, 0);
            case 0x14:
                this.unit = Unit.get(BaseUnit.VOLTAMPERE); // Channel value base unit is VA sum
                this.instantType = POWER;
                return new Pair<>(9, 0);
            case 0x15:
                dField = 0;
            case 0x16:
                dField = (dField == null) ? 11 : dField;
            case 0x17:
                dField = (dField == null) ? 22 : dField;
            case 0x18:
                dField = (dField == null) ? 10 : dField;
            case 0x19:
                dField = (dField == null) ? 20 : dField;
                this.unit = Unit.get(BaseUnit.DEGREE); // Channel value base unit is Voltage angle A-C
                this.instantType = ANGLE;
                return new Pair<>(81, dField);
            case 0x1A:
                dField = 1;
            case 0x1B:
                dField = (dField == null) ? 2 : dField;
            case 0x1C:
                dField = (dField == null) ? 3 : dField;
                this.unit = Unit.get(BaseUnit.UNITLESS); // Channel value base unit is Unbalanced Voltage 123 or THD Voltage angle A-C
                this.instantType = POWER;
                return new Pair<>(regFunction <= 0x09 ? 128 : 129, dField);
            default:
                throw new ProtocolException("Load survey channel definition contains invalid/unsupported instantaneous register (function '" + regFunction + "'" + ", value '" + regValue + "')");
        }
    }

    private Phase mapRegValueToPhase(int regValue, int offset) {
        int phaseValue = regValue - offset;
        if (phaseValue == 0) {
            return Phase.PHASE_A;
        } else if (phaseValue == 1) {
            return Phase.PHASE_B;
        } else if (phaseValue == 2) {
            return Phase.PHASE_C;
        } else {
            return Phase.TOTAL;
        }
    }

    private Pair<Integer, Integer> parseTestChannel(int regFunction, int regValue, int scaling) throws ProtocolException {
        this.ScalingFactor = 1;
        switch (regValue) {
            case 0x00:
                this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
                return new Pair<>(130, 1);
            case 0x01:
                this.unit = Unit.get(BaseUnit.KELVIN, scaling * 3); // Channel value base unit is Kelvin
                return new Pair<>(130, 2);
            case 0x02:
                this.unit = Unit.getUndefined();
                return new Pair<>(130, 3);
            default:
                throw new ProtocolException("Load survey channel definition contains invalid/unsupported test function register (function '" + regFunction + "'" + ", value '" + regValue + "')");
        }
    }

    private Pair<Integer, Integer> parsePulseChannel(int regValue, int scaling) {
        this.ScalingFactor = 10 ^ scaling;
        this.unit = Unit.getUndefined();
        return new Pair<>(82, regValue);
    }

    public int getDecimalPointPosition() {
        return decimalPointPosition;
    }

    public BigDecimal getScalingFactor() {
        return new BigDecimal(ScalingFactor);
    }

    public boolean isChannel() {
        return channelEnabled;
    }

    public Unit getUnit() {
        return unit;
    }

    public boolean isInstantaneous() {
        return this.instant;
    }

    public int getInstantaneousType() {
        return this.instantType;
    }

    public ObisCode getChannelObisCode() {
        return obisCode;
    }

    private boolean inRange(int regValue, Range range) {
        return regValue >= range.getFrom() && regValue <= range.getTo();

    }

    private int parseEnergyReadingToObisC(int regFunction, int regValue) throws ProtocolException {
        if (inRange(regValue, ACTIVE_POWER_RANGE)) {  // Active power
            switch (regFunction) {
                case ALL:
                    return 1;
                case POSITIVE:
                    return 1;
                case NEGATIVE:
                    return 2;
                case POSITIVE_Q1:
                    return 17;
                case NEGATIVE_Q2:
                    return 18;
                case NEGATIVE_Q3:
                    return 19;
                case POSITIVE_Q4:
                    return 20;
                default:
                    throw new ProtocolException("Load survey channel definition contains invalid/unsupported energy register (function '" + regFunction + "'" + ", value '" + regValue + "')");
            }
        } else if (inRange(regValue, REACTIVE_POWER_RANGE)) {   // Reactive power
            switch (regFunction) {
                case ALL:
                    return 3;
                case POSITIVE:
                    return 3;
                case NEGATIVE:
                    return 4;
                case POSITIVE_Q1:
                    return 5;
                case NEGATIVE_Q2:
                    return 6;
                case NEGATIVE_Q3:
                    return 7;
                case POSITIVE_Q4:
                    return 8;
                default:
                    throw new ProtocolException("Load survey channel definition contains invalid/unsupported energy register (function '" + regFunction + "'" + ", value '" + regValue + "')");
            }
        } else if (inRange(regValue, APPARENT_POWER_RANGE)) {  // Apparent power
            switch (regFunction) {
                case ALL:
                    return 9;
                case POSITIVE:
                    return 9;
                case NEGATIVE:
                    return 10;
                case POSITIVE_Q1:
                    return 138;
                case NEGATIVE_Q2:
                    return 134;
                case NEGATIVE_Q3:
                    return 142;
                case POSITIVE_Q4:
                    return 146;
                default:
                    throw new ProtocolException("Load survey channel definition contains invalid/unsupported energy register (function '" + regFunction + "'" + ", value '" + regValue + "')");
            }
        }
        throw new ProtocolException("Load survey channel definition contains invalid/unsupported energy register (function '" + regFunction + "'" + ", value '" + regValue + "')");
    }

    private void constructChannelObisCode(int baseObisC, Phase phase, Type type) {
        constructChannelObisCode(baseObisC, phase, type, 0);
    }

    private void constructChannelObisCode(int baseObisC, Phase phase, Type type, int eField) {
        this.obisCode = ObisCode.fromString(Utils.format("1.1.{0}.{1}.{2}.255", new Object[]{baseObisC + phase.getOffset(), type.getType(), eField}));
    }

    private enum Phase {
        NOT_APPLICABLE(0),
        TOTAL(0),
        PHASE_A(20),
        PHASE_B(40),
        PHASE_C(60);

        private final int offset;

        Phase(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }

    private enum Type {
        ACCUMULATED(8),
        MINIMUM(3),
        MAXIMUM(6),
        INSTANTANEOUS(7),
        AVERAGE(4);

        private final int type;

        Type(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        static Type fromTypeValue(int typeValue) {
            for (Type type : values()) {
                if (type.getType() == typeValue) {
                    return type;
                }
            }
            return ACCUMULATED;
        }
    }
}