package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 25/07/11
 * Time: 9:26
 */
public class ZigbeeGasRegisterFactory implements BulkRegisterProtocol {

    private ZigbeeGas zigbeeGas;

    public ZigbeeGasRegisterFactory(ZigbeeGas zigbeeGas) {
        this.zigbeeGas = zigbeeGas;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo(register.getObisCode().getDescription());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        // TODO: Read registers here
        return registerValues;
    }

    public ZigbeeGas getZigbeeGas() {
        return zigbeeGas;
    }

}
