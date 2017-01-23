package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 20/05/11
 * Time: 15:17
 */
public class StringValueFactory extends AbstractValueFactory {

    private final ProRegister proRegister;
    private final String value;

    public StringValueFactory(String obisCode, ProRegister proRegister, CewePrometer prometer) {
        super(ObisCode.fromString(obisCode), prometer);
        this.proRegister = proRegister;
        this.value = null;
    }

    public StringValueFactory(String obisCode, String value, CewePrometer prometer) {
        super(ObisCode.fromString(obisCode), prometer);
        this.proRegister = null;
        this.value = value;
    }

    @Override
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (proRegister != null) {
            return new RegisterValue(getObisCode(), getProRegister().asCompleteString());
        } else {
            return new RegisterValue(getObisCode(), value);
        }
    }

    @Override
    public Unit getUnit() {
        return Unit.getUndefined();
    }

    public ProRegister getProRegister() {
        return proRegister;
    }

    @Override
    public String getDescription() {
        return "";
    }

}
