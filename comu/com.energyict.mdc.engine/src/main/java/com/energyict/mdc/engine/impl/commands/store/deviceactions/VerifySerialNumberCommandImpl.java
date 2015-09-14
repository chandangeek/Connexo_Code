package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.VerifySerialNumberCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;

/**
 * Proper implementation for a {@link VerifySerialNumberCommand}
 *
 * @author gna
 * @since 31/05/12 - 16:06
 */
public class VerifySerialNumberCommandImpl extends SimpleComCommand implements VerifySerialNumberCommand {

    private final OfflineDevice offlineDevice;

    public VerifySerialNumberCommandImpl(final OfflineDevice offlineDevice, final CommandRoot commandRoot) {
        super(commandRoot);
        this.offlineDevice = offlineDevice;
    }

    /**
     * Perform the actions which are owned by this ComCommand.<br/>
     * <b>Note:</b> this action will only perform once
     * <p>
     * The serialNumber will be read from the device and verified with the serialNumber from the HeadEnd.
     * If the serialNumbers don't match, then a {@link DeviceConfigurationException} will be thrown.
     * </p>
     *
     * @param deviceProtocol the {@link DeviceProtocol} which will perform the actions
     * @param executionContext The ExecutionContext
     */
    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (!(MeterProtocolAdapter.class.isAssignableFrom(deviceProtocol.getClass()))) {
            String meterSerialNumber = deviceProtocol.getSerialNumber();
            if (!meterSerialNumber.equals(offlineDevice.getSerialNumber())) {
                addIssue(getIssueService().newProblem(getCommandType(), "CSC-CONF-112", meterSerialNumber, offlineDevice.getSerialNumber()), CompletionCode.ConfigurationError);
                throw DeviceConfigurationException.serialNumberMisMatch(meterSerialNumber, offlineDevice.getSerialNumber(), MessageSeeds.CONFIG_SERIAL_NUMBER_MISMATCH);
            }
        }
        else {
            addIssue(getIssueService().newWarning(deviceProtocol, "notPossibleToVerifySerialNumber", deviceProtocol.getSerialNumber(), deviceProtocol.getClass().getSimpleName()));
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Verify the device serial number";
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND;
    }

}