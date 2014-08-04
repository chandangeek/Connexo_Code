package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.identifiers.RegisterDataIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter between a {@link SmartMeterProtocol} and a {@link DeviceRegisterSupport}
 *
 * @author gna
 * @since 10/04/12 - 15:33
 */
public class SmartMeterProtocolRegisterAdapter implements DeviceRegisterSupport {

    private static final RegisterValue INVALID_REGISTER_VALUE = new RegisterValue(ObisCode.fromString("0.0.0.0.0.0"));

    /**
     * The used <code>SmartMeterProtocol</code> for which the adapter is working
     */
    private final SmartMeterProtocol smartMeterProtocol;
    private final IssueService issueService;

    public SmartMeterProtocolRegisterAdapter(final SmartMeterProtocol smartMeterProtocol, IssueService issueService) {
        this.smartMeterProtocol = smartMeterProtocol;
        this.issueService = issueService;
    }

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper {@link ResultType}
     * <b>and</b> {@link com.energyict.mdc.issues.Issue issue} should be returned so proper logging of this action can be performed.
     *
     * @param offlineRegisters The Registers for which to request a value
     * @return a <code>List</code> of collected register values
     */
    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> offlineRegisters) {
        if (offlineRegisters != null && this.smartMeterProtocol != null) {
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            List<Register> convertedRegisters = convertOfflineRegistersToRegister(offlineRegisters);
            try {
                CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
                final List<RegisterValue> registerValues = smartMeterProtocol.readRegisters(convertedRegisters);
                for (OfflineRegister register : offlineRegisters) {
                    RegisterValue registerValue = findRegisterValue(register, registerValues);
                    if(!registerValue.equals(INVALID_REGISTER_VALUE)){
                        CollectedRegister adapterDeviceRegister = collectedDataFactory.createCollectedRegisterForAdapter(getRegisterIdentifier(register), register.getReadingType());
                        adapterDeviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                        adapterDeviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                        collectedRegisters.add(adapterDeviceRegister);
                    } else {
                        CollectedRegister defaultDeviceRegister = collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
                        defaultDeviceRegister.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
                        collectedRegisters.add(defaultDeviceRegister);
                    }
                }
                return collectedRegisters;
            } catch (IOException e) {
                throw new LegacyProtocolException(e); // we should receive all the data or nothing
            }
        } else {
            return Collections.emptyList();
        }
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRegister){
        return new RegisterDataIdentifier(offlineRegister.getAmrRegisterObisCode(), offlineRegister.getObisCode(), offlineRegister.getDeviceIdentifier());
    }

    private List<Register> convertOfflineRegistersToRegister(final List<OfflineRegister> offlineRegisters) {
        List<Register> registers = new ArrayList<>(offlineRegisters.size());
        for (OfflineRegister offlineRegister : offlineRegisters) {
            registers.add(new Register((int) offlineRegister.getRegisterId(), offlineRegister.getObisCode(), offlineRegister.getSerialNumber()));
        }
        return registers;
    }

    /**
     * Find the requested RegisterValue corresponding to the given Register in the provided list
     *
     * @param register       the register to find
     * @param registerValues the list of registerValues
     * @return the requested registerValue
     */
    protected RegisterValue findRegisterValue(OfflineRegister register, List<RegisterValue> registerValues) {
        for (RegisterValue registerValue : registerValues) {
            if (registerValue.getObisCode().equals(register.getObisCode()) && registerValue.getSerialNumber().equals(register.getSerialNumber())) {
                return registerValue;
            }
        }
        return INVALID_REGISTER_VALUE;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}