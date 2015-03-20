package com.energyict.protocolimpl.modbus.generic;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.CoilStatusRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.InputRegister;
import com.energyict.protocolimpl.modbus.core.InputStatusRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ReportSlaveIDRegister;

import java.io.IOException;
import java.util.Map;

/**
 * @author sva
 * @since 18/11/13 - 14:25
 */
public class RegisterFactory extends AbstractRegisterFactory {

    private ParserFactory parserFactory;

    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    @Override
    protected void init() {
        setZeroBased(((Generic) getModBus()).isStartRegistersZeroBased()); // this means that reg2read = reg-1
    }

    @Override
    public AbstractRegister findRegister(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(), obc.getB(), obc.getC(), obc.getD(), obc.getE(), Math.abs(obc.getF()));
        RegisterDefinition registerDefinition = createRegisterDefinition(obisCode);

        Integer overruleRegister = getCustomStartRegisterMap().get(registerDefinition.getRegister());
        if (overruleRegister != null) { // Overrule the obis startRegister with the one from the custom property
            registerDefinition.setRegister(overruleRegister);
        }

        AbstractRegister register;

        switch (registerDefinition.getFunction()) {
            case READ_COIL_STATUS:
                register = new CoilStatusRegister(
                                        registerDefinition.getRegister(),
                                        registerDefinition.getDataTypeSelector().getRange(),
                                        obisCode,
                                        Unit.getUndefined(),
                                        "Coil status " + obisCode
                                );
                                register.setParser(Integer.toString(registerDefinition.getDataTypeSelector().getDataTypeSelectorCode()));
                                break;
            case READ_INPUT_STATUS:
                register = new InputStatusRegister(
                                        registerDefinition.getRegister(),
                                        registerDefinition.getDataTypeSelector().getRange(),
                                        obisCode,
                                        Unit.getUndefined(),
                                        "Input status " + obisCode
                                );
                                register.setParser(Integer.toString(registerDefinition.getDataTypeSelector().getDataTypeSelectorCode()));
                                break;
            case READ_HOLDING_REGISTERS:
                register = new HoldingRegister(
                        registerDefinition.getRegister(),
                        registerDefinition.getDataTypeSelector().getRange(),
                        obisCode,
                        Unit.getUndefined(),
                        "Holding register " + obisCode
                );
                register.setParser(Integer.toString(registerDefinition.getDataTypeSelector().getDataTypeSelectorCode()));
                break;
            case READ_INPUT_REGISTERS:
                register = new InputRegister(
                        registerDefinition.getRegister(),
                        registerDefinition.getDataTypeSelector().getRange(),
                        obisCode,
                        Unit.getUndefined(),
                        "Input register " + obisCode

                );
                register.setParser(Integer.toString(registerDefinition.getDataTypeSelector().getDataTypeSelectorCode()));
                break;
            case REPORT_SLAVE_ID:
                register = new ReportSlaveIDRegister(-1, -1, "Slave ID");
                break;

            case UNKNOWN:
            default:
                throw new NoSuchRegisterException("RegisterFactory, unknown modbus function " + obisCode.getB());
        }
        register.setRegisterFactory(this);
        return register;
    }

    protected RegisterDefinition createRegisterDefinition(ObisCode obisCode) {
        return new RegisterDefinition(obisCode);
    }

    @Override
    public ParserFactory getParserFactory() {
        if (parserFactory == null) {
            parserFactory = new ParserFactory();
        }
        return parserFactory;
    }

    protected void initParsers() {
        // No initialization needed
    }

    private Map<Integer, Integer> getCustomStartRegisterMap() {
        return ((Generic) getModBus()).getCustomStartRegisterMap();
    }
}
