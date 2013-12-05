package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.DataType;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;

/**
 * Class used for writing the alarm link settings.
 *
 * Copyrights EnergyICT
 * Date: 19-mei-2011
 * Time: 11:20:40
 */
public class AlarmLinks extends AbstractRegister {

    private int writeValue;

    public AlarmLinks(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.AlarmLinks.getId();
    }

    @Override
    protected void parse(byte[] data) throws IOException {
    }

    @Override
    protected byte[] getWriteASDU() {
        return ASDU.WriteRegister.getIdBytes();
    }

    @Override
    protected byte[] getWriteBytes() {
        byte[] request = new byte[2];
        request[0] = (byte) DataType.Byte.getId();
        request[1] = (byte) writeValue;
        return request;
    }

    public void setWriteValue(int writeValue) {
        this.writeValue = writeValue;
    }
}