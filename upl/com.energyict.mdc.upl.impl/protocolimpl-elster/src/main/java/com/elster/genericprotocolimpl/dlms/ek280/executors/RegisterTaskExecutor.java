package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights
 * Date: 10/06/11
 * Time: 10:30
 */
public class RegisterTaskExecutor extends AbstractExecutor<RegisterTaskExecutor.RegisterTask> {

    public RegisterTaskExecutor(AbstractExecutor executor) {
        super(executor);
    }

    @Override
    public void execute(RegisterTask registerTask) throws IOException {
        List<RtuRegister> registers = registerTask.getRegisters();
        for (RtuRegister register : registers) {
            ObisCode obis = register.getRtuRegisterSpec().getDeviceObisCode();
            if (obis == null) {
                obis = register.getRegisterMapping().getObisCode();
            }
            try {
                RegisterValue registerValue = getDlmsProtocol().readRegister(obis);
                getStoreObject().add(register, registerValue);
            } catch (NoSuchRegisterException e) {
                getMeterAmrLogging().logRegisterFailure(e, obis);
            }
        }
    }

    protected static class RegisterTask {

        private final List<RtuRegister> registers;

        public RegisterTask(List<RtuRegister> registers) {
            this.registers = registers;
        }

        public List<RtuRegister> getRegisters() {
            return registers;
        }

    }
}
