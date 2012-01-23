package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
            RtuRegisterSpec registerSpec = register.getRtuRegisterSpec();
            ObisCode obis = registerSpec.getDeviceObisCode();
            ObisCode obisToRead;
            if (obis == null) {
                obis = register.getRegisterMapping().getObisCode();
                obisToRead = ProtocolTools.setObisCodeField(obis, 1, (byte) (registerSpec.getDeviceChannelIndex() & 0x0FF));
            } else {
                obisToRead = ObisCode.fromByteArray(obis.getLN());
            }
            try {
                RegisterValue registerValue = getDlmsProtocol().readRegister(obisToRead);
                getStoreObject().add(register, ProtocolTools.setRegisterValueObisCode(registerValue, obis));
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
