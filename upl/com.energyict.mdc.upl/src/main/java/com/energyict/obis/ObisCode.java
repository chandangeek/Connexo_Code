/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.obis;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
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


    private int a;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private boolean relativeBillingPeriod;
    private boolean invalid = false;

    //needed for Flex synchronization

    public ObisCode() {
    }


    public ObisCode(int a, int b, int c, int d, int e, int f, boolean relativeBillingPeriod) {
        if (a < 0 || a > 255) {
            this.invalid = true;
        }
        if (b < -1 || b > 255) {
            this.invalid = true;
        }
        if (c < 0 || c > 255) {
            this.invalid = true;
        }
        if (d < 0 || d > 255) {
            this.invalid = true;
        }
        if (e < 0 || e > 255) {
            this.invalid = true;
        }
        if (relativeBillingPeriod) {
            if (f < -99 || f > 1) {
                this.invalid = true;
            }
        } else {
            if (f < 0 || f > 255) {
                this.invalid = true;
            }
        }
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.relativeBillingPeriod = relativeBillingPeriod;
    }

    public ObisCode(int a, int b, int c, int d, int e, int f) {
        this(a, b, c, d, e, f, false);
    }

    public ObisCode(ObisCode base, int channelIndex) {
        this(
                base.getA(),
                channelIndex,
                base.getC(),
                base.getD(),
                base.getE(),
                base.getF(),
                base.useRelativeBillingPeriod());
    }

    public ObisCode(ObisCode base, int channelIndex, int billingPeriodIndex) {
        this(
                base.getA(),
                channelIndex,
                base.getC(),
                base.getD(),
                base.getE(),
                base.useRelativeBillingPeriod() ?
                        ((billingPeriodIndex + base.getF()) % 100) :
                        base.getF(),
                false);
    }

    @XmlAttribute
    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public boolean useRelativeBillingPeriod() {
        return relativeBillingPeriod;
    }

    @XmlAttribute
    public boolean isRelativeBillingPeriod() {
        return relativeBillingPeriod;
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
        if (useRelativeBillingPeriod()) {
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

    public boolean equals(Object o) {
        return equalsSelectiveFieldsCheck(o, true, true, true, true, true, true, true);
    }

    public boolean equalsIgnoreBChannel(Object o) {
        return equalsSelectiveFieldsCheck(o, true, false, true, true, true, true, true);
    }

    public boolean equalsIgnoreBAndEChannel(Object o) {
        return equalsSelectiveFieldsCheck(o, true, false, true, true, false, true, true);
    }

    public boolean equalsIgnoreBillingField(Object o) {
        return equalsSelectiveFieldsCheck(o, true, true, true, true, true, false, true);
    }

    private boolean equalsSelectiveFieldsCheck(Object o, boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean relative) {
        if (o == null) {
            return false;
        }
        try {
            ObisCode other = (ObisCode) o;
            return
                    ((this.a == other.a) || !a) &&
                            ((this.b == other.b) || !b) &&
                            ((this.c == other.c) || !c) &&
                            ((this.d == other.d) || !d) &&
                            ((this.e == other.e) || !e) &&
                            ((this.f == other.f) || !f) &&
                            ((this.relativeBillingPeriod == other.relativeBillingPeriod) || !relative);
        } catch (ClassCastException ex) {
            return false;
        }
    }

    public int hashCode() {
        return toString().hashCode();
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
        return useRelativeBillingPeriod() && f == 1;
    }

    @JsonIgnore
    @XmlTransient
    public boolean isLastBillingPeriod() {
        return useRelativeBillingPeriod() && f == 0;
    }

    public boolean hasBillingPeriod() {
        return useRelativeBillingPeriod() || f < 100;
    }

    // KV 12102004

    public static ObisCode fromByteArray(byte[] ln) {
        boolean hasRelativeBillingPoint = false;
        int a = ln[0] & 0xFF;
        int b = ln[1] & 0xFF;
        int c = ln[2] & 0xFF;
        int d = ln[3] & 0xFF;
        int e = ln[4] & 0xFF;
        int f = ln[5] & 0xFF;
        return new ObisCode(a, b, c, d, e, f, hasRelativeBillingPoint);
    }

    public static ObisCode fromString(String codeString) {
        boolean invalid = false;
        List<String> obisFields = new ArrayList<>();
        if(codeString != null && codeString.length()>0) {
            obisFields = new ArrayList<>(Arrays.asList(codeString.split("\\."))).stream().map(s -> s.trim()).collect(Collectors.toList());
        }

        if(obisFields.size() != 6){
            invalid = true;
            while(obisFields.size() < 6){
                obisFields.add("0");
            }
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
            ObisCode o = new ObisCode(a, b, c, d, e, f, hasRelativeBillingPoint);
            if(invalid){
                o.setInvalid(invalid);
            }
            return o;
        } catch(Exception e) {
            ObisCode o = new ObisCode(0, 0, 0, 0, 0, 0, false);
            o.setInvalid(true);
            return o;
        }

    }

    private static String nextToken(StringTokenizer tokenizer) {
        return tokenizer.nextToken().trim();
    }

    // first 20 C field codes, applied to electricity related codes
    static Unit[] units = {Unit.get(""), // General purpose objects
            Unit.get(BaseUnit.WATT), // active import Q1+Q4
            Unit.get(BaseUnit.WATT), // active export Q2+Q3
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive import Q1+Q2
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive export Q3+Q4
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q1
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q2
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q3
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q4
            Unit.get(BaseUnit.VOLTAMPERE), // apparent import Q1+Q4 of Q1+Q2+Q3+Q4
            Unit.get(BaseUnit.VOLTAMPERE), // apparent export Q2+Q3
            Unit.get(BaseUnit.AMPERE), // current any phase
            Unit.get(BaseUnit.VOLT), // // voltage any phase
            Unit.get(""), // power factor
            Unit.get(BaseUnit.HERTZ), // supply frequency
            Unit.get(BaseUnit.WATT), // active power abs(Q1+Q4) + abs(Q2+Q3)
            Unit.get(BaseUnit.WATT), // active power abs(Q1+Q4) - abs(Q2+Q3)
            Unit.get(BaseUnit.WATT), // active Q1
            Unit.get(BaseUnit.WATT), // active Q2
            Unit.get(BaseUnit.WATT), // active Q3
            Unit.get(BaseUnit.WATT)}; // active Q4

    @JsonIgnore
    @XmlTransient
    public Unit getUnitElectricity(int scaler) {
        Unit unit = doGetUnitElectricity(scaler);
        if (((getD() >= 8) && (getD() <= 10)) || ((getD() >= 29) && (getD() <= 30))) {
            return unit.getVolumeUnit();
        } else {
            return unit;
        }
    }

    public Unit doGetUnitElectricity(int scaler) {
        Unit unit = Unit.get("");
        if (getC() == 0) {
            unit = Unit.get("");
        } else if ((getC() >= 0) && (getC() <= 20)) {
            unit = units[getC()];
        } else if ((getC() >= 21) && (getC() <= 40)) {
            unit = units[(getC() % 21) + 1];
        } else if ((getC() >= 41) && (getC() <= 60)) {
            unit = units[(getC() % 41) + 1];
        } else if ((getC() >= 61) && (getC() <= 80)) {
            unit = units[(getC() % 61) + 1];
        } else if (getC() == 81) // angles
        {
            unit = Unit.get(BaseUnit.DEGREE);
        } else if (getC() == 91) // angles
        {
            unit = Unit.get(BaseUnit.AMPERE);
        } else if (getC() == 92) // angles
        {
            unit = Unit.get(BaseUnit.VOLT);
        }

        return Unit.get(unit.getDlmsCode(), scaler);

    }

    @JsonIgnore
    @XmlTransient
    public String getValue() {
        return toString();
    }


    public void setValue(String value) throws ParseException {
        ObisCode obisCode = ObisCode.fromString(value);
        this.a = obisCode.getA();
        this.b = obisCode.getB();
        this.c = obisCode.getC();
        this.d = obisCode.getD();
        this.e = obisCode.getE();
        this.f = obisCode.getF();
        this.relativeBillingPeriod = obisCode.useRelativeBillingPeriod();
    }

    public ObisCode nextB () {
        return new ObisCode(this.a, this.b + 1, this.c, this.d, this.e, this.f);
    }

    public void setB(int b) {
        this.b = b;
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
