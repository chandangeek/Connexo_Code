package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.DataType;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read/write the billing configuration parameters
 * <p/>
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 9:23:31
 */
public class BillingParameters extends AbstractRegister {

    private int writeValue;

    public BillingParameters(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public BillingParameters(Poreg poreg) {
        super(poreg, 0, 0, poreg.isPoreg2() ? 32 : 64, 17);
    }

    private List<BankConfiguration> configs = new ArrayList<BankConfiguration>();

    public List<BankConfiguration> getConfigs() {
        return configs;
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.BillingParameters.getId();
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());

        int resultType;
        int resultRenewal;
        int tariffType;
        int tariffIndex;
        int resultLevel;
        int[] channels = new int[8];
        int offset = 0;

        for (int bankId = 0; bankId < 32; bankId++) {
            resultType = values.get(offset++).getValue();
            if (resultType == 0) {
                break;
            }
            offset++;
            resultRenewal = values.get(offset++).getValue();
            offset++;
            offset++;
            offset++;
            tariffType = values.get(offset++).getValue();
            tariffIndex = values.get(offset++).getValue();
            resultLevel = values.get(offset++).getValue();
            channels[0] = values.get(offset++).getValue();
            channels[1] = values.get(offset++).getValue();
            channels[2] = values.get(offset++).getValue();
            channels[3] = values.get(offset++).getValue();
            channels[4] = values.get(offset++).getValue();
            channels[5] = values.get(offset++).getValue();
            channels[6] = values.get(offset++).getValue();
            channels[7] = values.get(offset++).getValue();

            configs.add(new BankConfiguration(bankId, resultRenewal, channels, resultLevel, resultType, tariffIndex, tariffType));
        }
    }

    @Override
    protected byte[] getWriteASDU() {
        return ASDU.WriteRegister.getIdBytes();
    }

    @Override
    protected byte[] getWriteBytes() {
        byte[] request = new byte[getNumberOfRegisters() * 2];
        int index = 0;
        for (int i = 0; i < getNumberOfRegisters(); i++) {
            request[index++] = (byte) DataType.Byte.getId();
            request[index++] = (byte) writeValue;
        }
        return request;
    }

    public void setWriteValue(int writeValue) {
        this.writeValue = writeValue;
    }
}