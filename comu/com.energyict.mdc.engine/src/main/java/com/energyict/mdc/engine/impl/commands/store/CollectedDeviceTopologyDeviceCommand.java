package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceTopology;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectedDeviceTopologyDeviceCommand extends DeviceCommandImpl {

    private final DeviceTopology deviceTopology;
    private ComTaskExecution comTaskExecution;
    private boolean topologyChanged;
    private List<String> serialNumbersRemovedFromTopology = new ArrayList<>();
    private List<String> serialNumbersAddedToTopology = new ArrayList<>();

    public CollectedDeviceTopologyDeviceCommand(DeviceTopology deviceTopology, ComTaskExecution comTaskExecution, IssueService issueService, Clock clock) {
        super(issueService, clock);
        this.deviceTopology = deviceTopology;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        OfflineDevice device = comServerDAO.findDevice(deviceTopology.getDeviceIdentifier());
        this.topologyChanged = false;
        Map<String, OfflineDevice> oldSlavesBySerialNumber = this.mapOldSlavesToSerialNumber(device);
        Map<String, DeviceIdentifier> actualSlavesByDeviceId = this.mapActualSlavedToDeviceIdAndHandleUnknownDevices(comServerDAO);
        this.handleSlaveRemoval(comServerDAO, oldSlavesBySerialNumber, actualSlavesByDeviceId);
        this.handleSlaveMoves(comServerDAO, oldSlavesBySerialNumber, actualSlavesByDeviceId);
        if (this.topologyChanged && this.deviceTopology.getTopologyAction().equals(TopologyAction.VERIFY)) {
            DeviceIdentifier deviceFinder = deviceTopology.getDeviceIdentifier();
            comServerDAO.signalEvent(new DeviceTopologyChangedEvent(deviceFinder.findDevice(), deviceTopology.getSlaveDeviceIdentifiers()));
        }
        if (!this.serialNumbersRemovedFromTopology.isEmpty()) {
            String allSerials = getSerialNumbersAsString(this.serialNumbersRemovedFromTopology);
            getExecutionLogger().addIssue(
                    CompletionCode.ConfigurationWarning,
                    getIssueService().newWarning(deviceTopology, "serialsRemovedFromTopology", allSerials), comTaskExecution);
        }
        if (!this.serialNumbersAddedToTopology.isEmpty()) {
            String allSerials = getSerialNumbersAsString(this.serialNumbersAddedToTopology);
            getExecutionLogger().addIssue(
                    CompletionCode.ConfigurationWarning,
                    getIssueService().newWarning(deviceTopology, "serialsAddedToTopology", allSerials), comTaskExecution);
        }
    }

    private String getSerialNumbersAsString(List<String> allSerials) {
        StringBuilder serialBuilder = new StringBuilder();
        String separator = " - ";
        for (String serial : allSerials) {
            serialBuilder.append(serial);
            serialBuilder.append(separator);
        }
        return serialBuilder.substring(0, serialBuilder.lastIndexOf(separator));
    }

    private void handleSlaveMoves (ComServerDAO comServerDAO, Map<String, OfflineDevice> oldSlavesBySerialNumber, Map<String, DeviceIdentifier> actualSlavesByDeviceId) {
        Set<String> actualSerialNumbers = new HashSet<>(actualSlavesByDeviceId.keySet());
        actualSerialNumbers.removeAll(oldSlavesBySerialNumber.keySet());
        for (String actualSerialNumber : actualSerialNumbers) {
            this.handleMoveOfSlave(comServerDAO, actualSlavesByDeviceId.get(actualSerialNumber));
            this.serialNumbersAddedToTopology.add(actualSerialNumber);
            this.topologyChanged = true;
        }
    }

    private void handleSlaveRemoval (ComServerDAO comServerDAO, Map<String, OfflineDevice> oldSlavesBySerialNumber, Map<String, DeviceIdentifier> actualSlavesByDeviceId) {
        Set<String> oldSerialNumbers = new HashSet<>(oldSlavesBySerialNumber.keySet());
        oldSerialNumbers.removeAll(actualSlavesByDeviceId.keySet());
        for (String oldSerialNumber : oldSerialNumbers) {
            this.handleRemovalOfSlave(comServerDAO, oldSlavesBySerialNumber.get(oldSerialNumber));
            this.serialNumbersRemovedFromTopology.add(oldSerialNumber);
            this.topologyChanged = true;
        }
    }

    private Map<String, DeviceIdentifier> mapActualSlavedToDeviceIdAndHandleUnknownDevices (ComServerDAO comServerDAO) {
        Map<String, DeviceIdentifier> actualSlavesByDeviceId = new HashMap<>();
        List<DeviceIdentifier> actualSlaveDevices = deviceTopology.getSlaveDeviceIdentifiers();
        for (DeviceIdentifier slaveId : actualSlaveDevices) {
            OfflineDevice slave = comServerDAO.findDevice(slaveId);
            if (slave != null) {
                actualSlavesByDeviceId.put(slave.getSerialNumber(), slaveId);
            }
            else {
                this.handleAdditionOfSlave(comServerDAO, slaveId);
                this.serialNumbersAddedToTopology.add(slaveId.getIdentifier());
                this.topologyChanged = true;
            }
        }
        return actualSlavesByDeviceId;
    }

    private Map<String, OfflineDevice> mapOldSlavesToSerialNumber (OfflineDevice device) {
        Map<String, OfflineDevice> oldSlavesBySerialNumber = new HashMap<>();
        List<OfflineDevice> oldSlaveDevices = device.getAllSlaveDevices();
        for (OfflineDevice slave : oldSlaveDevices) {
            oldSlavesBySerialNumber.put(slave.getSerialNumber(), slave);
        }
        return oldSlavesBySerialNumber;
    }

    /**
     * Method to execute when we detect a removed slave device.
     * In this case the gateway of the slave device - in EIServer - still points to the master, however in the real world the slave has been removed.
     *
     * @param comServerDAO the ComServerDAO to be used
     * @param removedSlave the slave that is removed and for which we should clear its gateway
     */
    private void handleRemovalOfSlave(ComServerDAO comServerDAO, OfflineDevice removedSlave) {
        if (deviceTopology.getTopologyAction() == TopologyAction.UPDATE) {
            comServerDAO.updateGateway(new SerialNumberDeviceIdentifier(removedSlave.getSerialNumber()), null);
        }
    }

    /**
     * Method to execute when we detect a new slave device has been added to the device.
     * In this case a new slave device - unknown to EIServer - has been added to the device.
     *
     * @param comServerDAO the ComServerDAO to be used
     * @param addedSlave   the new slave that has been added to the device
     */
    private void handleAdditionOfSlave(ComServerDAO comServerDAO, DeviceIdentifier addedSlave) {
        DeviceIdentifier deviceFinder = deviceTopology.getDeviceIdentifier();
        comServerDAO.signalEvent(new UnknownSlaveDeviceEvent(deviceFinder.findDevice(), addedSlave));
    }

    /**
     * Method to execute when we detect an existing slave device being moved to another master.
     * In this case in the real world the slave device has been moved to a new master, but EIServer is not yet aware of this move.
     *
     * @param comServerDAO the ComServerDAO to be used
     * @param movedSlave   the slave that is moved and for which we should update its gateway
     */
    private void handleMoveOfSlave(ComServerDAO comServerDAO, DeviceIdentifier movedSlave) {
        if (deviceTopology.getTopologyAction() == TopologyAction.UPDATE) {
            comServerDAO.updateGateway(movedSlave, deviceTopology.getDeviceIdentifier());
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addLabel(this.deviceTopology.getTopologyAction().toString());
            builder.addProperty("deviceIdentifier").append(this.deviceTopology.getDeviceIdentifier());
        }
    }
}