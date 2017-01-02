package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceRegisterReadingNotSupported;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter between a {@link RegisterProtocol} and a {@link DeviceRegisterSupport}
 *
 * @author gna
 * @since 4/04/12 - 15:33
 */
public class MeterProtocolRegisterAdapter implements DeviceRegisterSupport {

    /**
     * The used <code>RegisterProtocol</code> for which the adapter is working
     */
    private final RegisterProtocol registerProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    public MeterProtocolRegisterAdapter(final RegisterProtocol registerProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        if (registerProtocol != null) {
            this.registerProtocol = registerProtocol;
        } else {
            this.registerProtocol = new DeviceRegisterReadingNotSupported();
        }
    }

    /**
     * Collect the values of the given <code>Registers</code>.
     * If for some reason the <code>Register</code> is not supported, a proper {@link ResultType}
     * <b>and</b> {@link com.energyict.mdc.upl.issue.Issue issue} should be returned so proper logging of this action can be performed.
     *
     * @param registers The Registers for which to request a value
     * @return a <code>List</code> of collected register values
     */
    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> registers) {
        if (registers != null) {
            CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (OfflineRegister register : registers) {
                try {
                    RegisterValue registerValue = this.registerProtocol.readRegister(register.getObisCode());
                    CollectedRegister adapterDeviceRegister = collectedDataFactory.createCollectedRegisterForAdapter(register.getRegisterIdentifier(), register.getReadingTypeMRID());
                    adapterDeviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    adapterDeviceRegister.setCollectedTimeStamps(
                            registerValue.getReadTime(),
                            registerValue.getFromTime(),
                            registerValue.getToTime(),
                            registerValue.getEventTime());
                    collectedRegisters.add(adapterDeviceRegister);
                } catch (UnsupportedException | NoSuchRegisterException e) {
                    CollectedRegister defaultDeviceRegister = collectedDataFactory.createDefaultCollectedRegister(register.getRegisterIdentifier(), register.getReadingTypeMRID());
                    defaultDeviceRegister.setFailureInformation(ResultType.NotSupported, this.issueService.newProblem(register.getObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.REGISTER_NOT_SUPPORTED, register.getObisCode()));
                    collectedRegisters.add(defaultDeviceRegister);
                }
                catch (IOException e) {
                    throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
                }
            }
            return collectedRegisters;
        } else {
            return Collections.emptyList();
        }
    }

    public RegisterProtocol getRegisterProtocol() {
        return registerProtocol;
    }
}