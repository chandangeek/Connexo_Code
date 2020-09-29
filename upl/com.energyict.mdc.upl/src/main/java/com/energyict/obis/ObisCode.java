/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.obis;

import com.energyict.cbo.Unit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public class ObisCode implements Serializable {

    public static final int CODE_D_CUMULATIVE_MAXUMUM_DEMAND = 2;
    public static final int CODE_D_RISING_DEMAND = 4;
    public static final int CODE_D_LAST_AVERAGE = 5;
    public static final int CODE_D_MAXIMUM_DEMAND = 6;
    public static final int CODE_D_MINIMUM = 3;
    public static final int CODE_D_TIME_INTEGRAL = 8; // keep for compatibility reasons
    public static final int CODE_D_TIME_INTEGRAL1 = 8;
    public static final int CODE_D_CURRENT_AVERAGE5 = 27;
    public static final int CODE_D_TIME_INTEGRAL5 = 29;
    public static final int CODE_D_INSTANTANEOUS = 7;

    public static final int CODE_C_ACTIVE_IMPORT = 1;
    public static final int CODE_C_ACTIVE_EXPORT = 2;
    public static final int CODE_C_REACTIVE_IMPORT = 3;
    public static final int CODE_C_REACTIVE_EXPORT = 4;
    public static final int CODE_C_REACTIVE_Q1 = 5;
    public static final int CODE_C_REACTIVE_Q2 = 6;
    public static final int CODE_C_REACTIVE_Q3 = 7;
    public static final int CODE_C_REACTIVE_Q4 = 8;
    public static final int CODE_C_APPARENT = 9;
    public static final int CODE_C_CURRENTANYPHASE = 11;
    public static final int CODE_C_VOLTAGEANYPHASE = 12;
    public static final int CODE_C_POWERFACTOR = 13;
    public static final int CODE_C_UNITLESS = 82;

    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final int e;
    private final int f;
    private final boolean relativeBillingPeriod;
    private final boolean invalid;

    public ObisCode() {
        this(0,0,0,0,0,0, false, true);
    }

    public ObisCode(int a, int b, int c, int d, int e, int f, boolean relativeBillingPeriod) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.relativeBillingPeriod = relativeBillingPeriod;
        this.invalid = validate(a, b, c, d, e, f, relativeBillingPeriod);
    }

    public ObisCode(int a, int b, int c, int d, int e, int f, boolean relativeBillingPeriod, boolean forceInvalid) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.relativeBillingPeriod = relativeBillingPeriod;
        this.invalid = forceInvalid? true:  validate(a, b, c, d, e, f, relativeBillingPeriod);
    }

    public ObisCode(int a, int b, int c, int d, int e, int f) {
        this(a, b, c, d, e, f, false, false);
    }

    public ObisCode(ObisCode base, int channelIndex) {
        this(
                base.getA(),
                channelIndex,
                base.getC(),
                base.getD(),
                base.getE(),
                base.getF(),
                base.isRelativeBillingPeriod(), false);
    }

    public ObisCode(ObisCode base, int channelIndex, int billingPeriodIndex) {
        this(
                base.getA(),
                channelIndex,
                base.getC(),
                base.getD(),
                base.getE(),
                base.isCurrentBillingPeriod() ?
                        ((billingPeriodIndex + base.getF()) % 100) :
                        base.getF(),
                base.isRelativeBillingPeriod(), false);
    }

    private boolean validate(int a, int b, int c, int d, int e, int f, boolean relativeBillingPeriod) {
        if (a < 0 || a > 255) {
            return true;
        }
        if (b < -1 || b > 255) {
            return true;
        }
        if (c < 0 || c > 255) {
            return true;
        }
        if (d < 0 || d > 255) {
            return true;
        }
        if (e < 0 || e > 255) {
            return true;
        }
        if (relativeBillingPeriod) {
            if (f < -99 || f > 1) {
                return true;
            }
        } else {
            if (f < 0 || f > 255) {
                return true;
            }
        }
        return false;
    }

    @XmlAttribute
    public boolean isInvalid() {
        return invalid;
    }

    @XmlAttribute
    public boolean isRelativeBillingPeriod() {
        return relativeBillingPeriod;
    }

    public boolean equals(ObisCode o) {
        return equalsSelectiveFieldsCheck(o, true, true, true, true, true, true, true);
    }

    public boolean equalsIgnoreBChannel(ObisCode o) {
        return equalsSelectiveFieldsCheck(o, true, false, true, true, true, true, true);
    }

    public boolean equalsIgnoreBAndEChannel(ObisCode o) {
        return equalsSelectiveFieldsCheck(o, true, false, true, true, false, true, true);
    }

    public boolean equalsIgnoreBillingField(ObisCode o) {
        return equalsSelectiveFieldsCheck(o, true, true, true, true, true, false, true);
    }

    private boolean equalsSelectiveFieldsCheck(ObisCode o, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean relative) {
        if (o == null) {
            return false;
        }
        return
                ((this.a == o.a) || !a) &&
                        ((this.b == o.b) || !b) &&
                        ((this.c == o.c) || !c) &&
                        ((this.d == o.d) || !d) &&
                        ((this.e == o.e) || !e) &&
                        ((this.f == o.f) || !f) &&
                        ((this.relativeBillingPeriod == o.relativeBillingPeriod) || !relative);
    }

    /**
     * Is the B-field an adjustable field?
     *
     * @return true if B is adjustable, false otherwise
     */
    public boolean anyChannel() {
        return b == -1;
    }

    @JsonIgnore
    @XmlTransient
    public byte[] getLN() {
        return new byte[]{(byte) getA(), (byte) getB(), (byte) getC(), (byte) getD(), (byte) getE(), (byte) getF()};
    }

    @XmlAttribute
    public int getA() {
        return a;
    }

    @XmlAttribute
    public int getB() {
        return b;
    }

    @XmlAttribute
    public int getC() {
        return c;
    }

    @XmlAttribute
    public int getD() {
        return d;
    }

    @XmlAttribute
    public int getE() {
        return e;
    }

    @XmlAttribute
    public int getF() {
        return f;
    }

    @JsonIgnore
    @XmlTransient
    public boolean isCurrentBillingPeriod() {
        return isLastBillingPeriod() && f == 1;
    }

    @JsonIgnore
    @XmlTransient
    public boolean isLastBillingPeriod() {
        return isCurrentBillingPeriod() && f == 0;
    }

    public boolean hasBillingPeriod() {
        return isRelativeBillingPeriod() || f < 100;
    }

    @JsonIgnore
    @XmlTransient
    public Unit getUnitElectricity(int scaler) {
        return ObisCodeUnitMapper.getUnitElectricity(this, scaler);
    }

    @JsonIgnore
    @XmlTransient
    public String getValue() {
        return toString();
    }

    public ObisCode nextB () {
        return new ObisCode(this.a, this.b + 1, this.c, this.d, this.e, this.f, this.relativeBillingPeriod, false);
    }

    public ObisCode setB(int b) {
        return new ObisCode(this.a, this.b, this.c, this.d, this.e, this.f, this.relativeBillingPeriod, false);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(a);
        buffer.append(".");
        if (b < 0) {
            buffer.append("x");
        } else {
            buffer.append(b);
        }
        buffer.append(".");
        buffer.append(c);
        buffer.append(".");
        buffer.append(d);
        buffer.append(".");
        buffer.append(e);
        buffer.append(".");
        if (isRelativeBillingPeriod()) {
            buffer.append("VZ");
            if (f > 0) {
                buffer.append("+");
                buffer.append(f);
            }
            if (f < 0) {
                buffer.append(f);
            }
        } else {
            buffer.append(f);
        }
        return buffer.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObisCode obisCode = (ObisCode) o;
        return a == obisCode.a &&
                b == obisCode.b &&
                c == obisCode.c &&
                d == obisCode.d &&
                e == obisCode.e &&
                f == obisCode.f &&
                relativeBillingPeriod == obisCode.relativeBillingPeriod &&
                invalid == obisCode.invalid;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    // all build methods should/can be extracted to a builder class: this needs to be done when all peer developers are aware since it affects most of the classes
    public static ObisCode fromByteArray(byte[] ln) {
        int a = ln[0] & 0xFF;
        int b = ln[1] & 0xFF;
        int c = ln[2] & 0xFF;
        int d = ln[3] & 0xFF;
        int e = ln[4] & 0xFF;
        int f = ln[5] & 0xFF;
        return new ObisCode(a, b, c, d, e, f, false, false);
    }

    public static ObisCode fromString(String codeString) {
        List<String> obisFields = new ArrayList<>();
        if(codeString != null && codeString.length()>0) {
            obisFields = new ArrayList<>(Arrays.asList(codeString.split("\\."))).stream().map(s -> s.trim()).collect(Collectors.toList());
        }

        if(obisFields.size() != 6){
            return new ObisCode();
        }
        try {
            int a = Integer.parseInt(obisFields.get(0));
            int b = "x".equalsIgnoreCase(obisFields.get(1)) ? -1 : Integer.parseInt(obisFields.get(1));
            int c = Integer.parseInt(obisFields.get(2));
            int d = Integer.parseInt(obisFields.get(3));
            int e = Integer.parseInt(obisFields.get(4));
            boolean hasRelativeBillingPoint = obisFields.get(5).startsWith("VZ");
            int f;
            if (hasRelativeBillingPoint) {
                if (obisFields.get(5).trim().length() == 2) {
                    f = 0;
                } else {
                    String billingPointOffset = obisFields.get(5).substring(2).trim();
                    if (billingPointOffset.startsWith("+")) {
                        billingPointOffset = billingPointOffset.substring(1);
                    }
                    f = Integer.parseInt(billingPointOffset);
                }
            } else {
                f = Integer.parseInt(obisFields.get(5));
            }
            return new ObisCode(a, b, c, d, e, f, hasRelativeBillingPoint, false);
        } catch(Exception e) {
            return new ObisCode();
        }

    }


    public static ObisCode setFieldAndGet(ObisCode obisCode, int fieldNo, int value) {
        final String[] obisLetters = obisCode.toString().split("\\.");
        final String letter = String.valueOf(value);

        switch (fieldNo) {
            case 1:
                obisLetters[0] = letter;
                break;
            case 2:
                obisLetters[1] = letter;
                break;
            case 3:
                obisLetters[2] = letter;
                break;
            case 4:
                obisLetters[3] = letter;
                break;
            case 5:
                obisLetters[4] = letter;
                break;
            case 6:
                obisLetters[5] = letter;
                break;
            default:
                break;
        }

        return ObisCode.fromString( String.join(".", obisLetters) );
    }
}
