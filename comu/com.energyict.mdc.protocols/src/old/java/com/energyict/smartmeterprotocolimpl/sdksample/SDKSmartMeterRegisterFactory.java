package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the registers in the virtual sdk setup:
 *
 * <pre>
 *  _________
 * |         |  Active Import register total:  1.0.1.8.0.255
 * | Master  |  Active Import register rate 1: 1.0.1.8.1.255
 * | 6 regs  |  Active Import register rate 2: 1.0.1.8.2.255
 * |         |
 * |         |  Active Export register total:  1.0.2.8.0.255
 * |         |  Active Export register rate 1: 1.0.2.8.1.255
 * |_________|  Active Export register rate 2: 1.0.2.8.2.255
 *      |      _________
 *      |---->|         |  Gas consumption register: 0.x.24.2.0.255
 *      |     | Slave1  |
 *      |     | 1 reg   |
 *      |     |_________|
 *      |
 *      |      _________
 *      |---->|         |  Gas consumption register: 0.x.24.2.0.255
 *            | Slave2  |
 *            | 1 reg   |
 *            |_________|
 * </pre>
 * Copyrights EnergyICT
 * Date: 9-feb-2011
 * Time: 13:41:09
 */
public class SDKSmartMeterRegisterFactory implements BulkRegisterProtocol {

    private static final String MASTER_SERIAL_NUMBER = "Master";
    private static final String SLAVE1_SERIAL_NUMBER = "Slave1";
    private static final String SLAVE2_SERIAL_NUMBER = "Slave2";

/*
0.x.24.2.0.255
*/
    private static final SDKSmartMeterRegister[] REGISTERS = new SDKSmartMeterRegister [] {
            new SDKSmartMeterRegister("1.0.1.8.0.255", MASTER_SERIAL_NUMBER, "kWh", 12000, 3),
            new SDKSmartMeterRegister("1.0.1.8.1.255", MASTER_SERIAL_NUMBER, "kWh", 18000, 3),
            new SDKSmartMeterRegister("1.0.1.8.2.255", MASTER_SERIAL_NUMBER, "kWh", 20000, 3),
            new SDKSmartMeterRegister("1.0.2.8.0.255", MASTER_SERIAL_NUMBER, "kWh", 24000, 2),
            new SDKSmartMeterRegister("1.0.2.8.1.255", MASTER_SERIAL_NUMBER, "kWh", 36000, 2),
            new SDKSmartMeterRegister("1.0.2.8.2.255", MASTER_SERIAL_NUMBER, "kWh", 40000, 2),

            new SDKSmartMeterRegister("0.x.24.2.0.255", SLAVE1_SERIAL_NUMBER, "m3", 140000, 3),
            new SDKSmartMeterRegister("0.x.24.2.0.255", SLAVE2_SERIAL_NUMBER, "m3", 290000, 3),

    };

    public RegisterInfo translateRegister(Register register) throws IOException {
        return findRegister(register).getRegisterInfo();
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        for (Register register : registers) {
            try {
                RegisterValue registerValue = findRegister(register).getRegisterValue();
                registerValues.add(registerValue);
            } catch (NoSuchRegisterException e) {
                // Do nothing
            }
        }
        return registerValues;
    }

    private SDKSmartMeterRegister findRegister(Register register) throws NoSuchRegisterException {
        for (SDKSmartMeterRegister meterRegister : REGISTERS) {
            if (meterRegister.getRegister().equals(register)) {
                return meterRegister;
            }
        }
        throw new NoSuchRegisterException("Register [" + register.toString() + "] not supported");
    }

}
