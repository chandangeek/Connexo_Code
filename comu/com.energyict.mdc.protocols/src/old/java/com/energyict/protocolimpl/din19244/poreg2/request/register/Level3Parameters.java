package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to read out the level 3 configuration settings.
 *
 * Copyrights EnergyICT
 * Date: 5-mei-2011
 * Time: 10:52:24
 */
public class Level3Parameters extends AbstractRegister {

    public Level3Parameters(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public Level3Parameters(Poreg poreg) {
        super(poreg, 0, 0, poreg.isPoreg2() ? 8 : 16, 1);           //Only read out field 0. The others aren't relevant.
    }

    private List<ExtendedValue> values = new ArrayList<ExtendedValue>();

    public List<ExtendedValue> getValues() {
        return values;
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.Level3Parameters.getId();
    }

    @Override
    protected void parse(byte[] data) {
        values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
    }
}