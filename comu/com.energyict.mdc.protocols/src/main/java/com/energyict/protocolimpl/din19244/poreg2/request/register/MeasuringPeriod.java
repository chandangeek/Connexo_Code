package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.List;

/**
 * Class to read out the measuring periods register.
 *
 * Copyrights EnergyICT
 * Date: 5-mei-2011
 * Time: 10:52:24
 */
public class MeasuringPeriod extends AbstractRegister {

    public MeasuringPeriod(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public MeasuringPeriod(Poreg poreg) {
        super(poreg, 0, 0, 1, 3);
    }

    private int[] periodsInSeconds = new int[3];

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.MeasuringPeriod.getId();
    }

    public int[] getPeriodsInSeconds() {
        return periodsInSeconds;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
        periodsInSeconds[0] = values.get(0).getValue();
        periodsInSeconds[1] = values.get(1).getValue();
        periodsInSeconds[2] = values.get(2).getValue();
    }
}