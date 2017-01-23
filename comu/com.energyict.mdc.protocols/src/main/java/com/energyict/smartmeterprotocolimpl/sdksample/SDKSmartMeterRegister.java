package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Copyrights EnergyICT
 * Date: 9-feb-2011
 * Time: 14:57:57
 */
public class SDKSmartMeterRegister {

    private final Register register;
    private final Unit unit;
    private final int divider;
    private final int digits;
    private final String description;

    public SDKSmartMeterRegister(String obisCode, String serialNumber, String unit, int divider, int digits, String description) {
        this.register = new Register(-1, ObisCode.fromString(obisCode), serialNumber);
        this.description = description;
        this.divider = divider;
        this.digits = digits;
        this.unit = Unit.get(unit);
    }

    public SDKSmartMeterRegister(String obisCode, String serialNumber, String unit, int divider, int digits) {
        this.register = new Register(-1, ObisCode.fromString(obisCode), serialNumber);
        this.description = register.getObisCode().getDescription();
        this.divider = divider;
        this.digits = digits;
        this.unit = Unit.get(unit);
    }

    public SDKSmartMeterRegister(String obisCode, String serialNumber, String unit, int divider) {
        this.register = new Register(-1, ObisCode.fromString(obisCode), serialNumber);
        this.description = register.getObisCode().getDescription();
        this.divider = divider;
        this.digits = 2;
        this.unit = Unit.get(unit);
    }

    public String getDescription() {
        return description;
    }

    public Register getRegister() {
        return register;
    }

    public int getDivider() {
        return divider;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getDigits() {
        return digits;
    }

    public RegisterInfo getRegisterInfo() {
        return new RegisterInfo(getDescription());
    }

    public RegisterValue getRegisterValue() {
        return  new RegisterValue(getRegister(), getQuantity());
    }

    private Quantity getQuantity() {
        return new Quantity(getValue(), getUnit());

    }

    private BigDecimal getValue() {
        BigDecimal value = BigDecimal.valueOf(System.currentTimeMillis());
        value = value.divide(BigDecimal.valueOf(getDivider()), RoundingMode.HALF_DOWN);
        value = value.movePointLeft(getDigits());
        return value;
    }

}
