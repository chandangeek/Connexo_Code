/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.DeviceTopologyChangedEvent;
import com.energyict.mdc.engine.impl.events.UnknownSlaveDeviceEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedDeviceTopologyEvent;
import com.energyict.mdc.engine.impl.meterdata.CollectedDeviceData;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.tasks.TopologyAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

public class CollectedDeviceTopologyDeviceCommand extends DeviceCommandImpl<CollectedDeviceTopologyEvent> {

    public static final String DESCRIPTION_TITLE = "Collected device topology";

    private final CollectedTopology deviceTopology;
    private final MeterDataStoreCommand meterDataStoreCommand;
    private boolean topologyChanged;

    /**
     * List containing the serial numbers of all slave devices removed from the device.
     */
    private List<String> serialNumbersRemovedFromTopology = new ArrayList<>();

    /**
     * List containing the serial numbers of all unknown slave devices (not yet present in EIServer) who are added to the device.
     */
    private List<String> unknownSerialNumbersAddedToTopology = new ArrayList<>();

    /**
     * List containing the serial numbers of all known slave devices (present in EIServer, but not linked to this device) who are added to the device.
     */
    private List<String> knownSerialNumbersAddedToTopology = new ArrayList<>();
    private com.energyict.mdc.protocol.api.device.offline.OfflineDevice offlineMasterDevice;

    public CollectedDeviceTopologyDeviceCommand(CollectedTopology deviceTopology, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceTopology = deviceTopology;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void logExecutionWith(ExecutionLogger logger) {
        super.logExecutionWith(logger);
        if (deviceTopology != null) {
            deviceTopology.getAdditionalCollectedDeviceInfo()
                    .stream()
                    .filter(x -> x instanceof CollectedDeviceData)
                    .map(CollectedDeviceData.class::cast)
                    .map(y -> y.toDeviceCommand(this.meterDataStoreCommand, this.getServiceProvider()))
                    .forEach(x -> x.logExecutionWith(logger));
        }
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        try {
            Optional<com.energyict.mdc.protocol.api.device.offline.OfflineDevice> device = comServerDAO.findOfflineDevice(deviceTopology.getDeviceIdentifier(), new DeviceOfflineFlags(SLAVE_DEVICES_FLAG));
            if (device.isPresent()) {
                this.offlineMasterDevice = device.get();
                this.topologyChanged = false;
                try {
                    handlePhysicalTopologyUpdate(comServerDAO, device.get());
                    signalTopologyChangedEvent(comServerDAO);
                    updateLogging();
                    handleAdditionalInformation(comServerDAO);
                } catch (CanNotFindForIdentifier e) {
                    this.addIssue(
                            CompletionCode.ConfigurationWarning,
                            getIssueService().newProblem(deviceTopology, e.getMessageSeed(), e.getMessageArguments()));
                }
            } else {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, MessageSeeds.COLLECTED_DEVICE_TOPOLOGY_FOR_UN_KNOWN_DEVICE, deviceTopology.getDeviceIdentifier()));
            }
        } catch (MacException e) {
            this.addIssue(CompletionCode.UnexpectedError,
                    getIssueService().newProblem(deviceTopology, MessageSeeds.MAC_CHECK_FAILURE));
        }
    }

    private void handleAdditionalInformation(ComServerDAO comServerDAO) {
        if (!isVerifyTopologyAction()) {
            doExecuteCollectedDeviceInfoCommands(comServerDAO);
            doStorePathSegments(comServerDAO);
            doStoreNeighbours(comServerDAO);
            doStoreG3DeviceAddressInformation(comServerDAO);
        }
    }

    private void doStoreG3DeviceAddressInformation(ComServerDAO comServerDAO) {
        G3TopologyDeviceAddressInformation g3TopologyDeviceAddressInformation = this.deviceTopology.getG3TopologyDeviceAddressInformation();
        if (g3TopologyDeviceAddressInformation != null) {
            try {
                comServerDAO.storeG3IdentificationInformation(g3TopologyDeviceAddressInformation);
            } catch (CanNotFindForIdentifier e) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, e.getMessageSeed(), g3TopologyDeviceAddressInformation.getDeviceIdentifier()));
            }
        }
    }

    private void doStoreNeighbours(ComServerDAO comServerDAO) {
        if (this.deviceTopology.getTopologyNeighbours().size() >= 1) {
            comServerDAO.storeNeighbours(this.deviceTopology.getDeviceIdentifier(), this.deviceTopology.getTopologyNeighbours());
        }
    }

    private void doStorePathSegments(ComServerDAO comServerDAO) {
        comServerDAO.storePathSegments(this.deviceTopology.getTopologyPathSegments());
    }

    private void doExecuteCollectedDeviceInfoCommands(ComServerDAO comServerDAO) {
        deviceTopology.getAdditionalCollectedDeviceInfo()
                .stream()
                .filter(x -> x instanceof CollectedDeviceData)
                .map(CollectedDeviceData.class::cast)
                .map(y -> y.toDeviceCommand(this.meterDataStoreCommand, this.getServiceProvider()))
                .forEach(x -> x.execute(comServerDAO));
    }

    private void updateLogging() {
        if (!this.serialNumbersRemovedFromTopology.isEmpty()) {
            String allSerials = getSerialNumbersAsString(this.serialNumbersRemovedFromTopology);
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    getIssueService().newWarning(deviceTopology, MessageSeeds.SERIALS_REMOVED_FROM_TOPOLOGY, allSerials));
        }
        if (!this.knownSerialNumbersAddedToTopology.isEmpty()) {
            String allSerials = getSerialNumbersAsString(this.knownSerialNumbersAddedToTopology);
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    getIssueService().newWarning(deviceTopology, MessageSeeds.SERIALS_ADDED_TO_TOPOLOGY, allSerials));
        }
        if (!this.unknownSerialNumbersAddedToTopology.isEmpty()) {
            String allSerials = getSerialNumbersAsString(this.unknownSerialNumbersAddedToTopology);
            if (isVerifyTopologyAction()) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newWarning(deviceTopology, MessageSeeds.UNKNOWN_SERIALS_ADDED_TO_TOPOLOGY, allSerials));
            } else {
                this.addIssue(
                        CompletionCode.ConfigurationError,
                        getIssueService().newProblem(deviceTopology, MessageSeeds.UNKNOWN_SERIALS_ADDED_TO_TOPOLOGY, allSerials));
            }
        }
    }

    private void signalTopologyChangedEvent(ComServerDAO comServerDAO) {
        /*
        CXO-12225 - as per request from product management and project team
         */
        if (this.topologyChanged && (isVerifyTopologyAction() || isUpdateTopologyAction())) {
            DeviceIdentifier deviceFinder = deviceTopology.getDeviceIdentifier();
            comServerDAO.signalEvent(EventType.DEVICE_TOPOLOGY_CHANGED.topic(), new DeviceTopologyChangedEvent(deviceFinder, deviceTopology.getSlaveDeviceIdentifiers().keySet()));
        }
    }

    private void handlePhysicalTopologyUpdate(ComServerDAO comServerDAO, OfflineDevice device) {
        Map<String, OfflineDevice> oldSlavesBySerialNumber = this.mapOldSlavesToSerialNumber(device);
        Map<String, DeviceIdentifier> actualSlavesByDeviceId = this.mapActualSlavedToDeviceIdAndHandleUnknownDevices(comServerDAO);
        Map<String, DeviceIdentifier> removedSlavesByDeviceId = this.mapRemovedSlavesToSerialNumber(comServerDAO);

        if (deviceTopology.getJoinedSlaveDeviceIdentifiers() != null) {
            this.processJoinedSlaves(comServerDAO);
        } else {
            //the actual slaves list in case that some devices have been removed
            if (actualSlavesByDeviceId.isEmpty() && !removedSlavesByDeviceId.isEmpty()) {
                actualSlavesByDeviceId.putAll(oldSlavesBySerialNumber.entrySet().stream()
                        .filter(item -> !removedSlavesByDeviceId.containsKey(item.getKey()))
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getDeviceIdentifier())));
            }

            this.handleSlaveRemoval(comServerDAO, oldSlavesBySerialNumber, actualSlavesByDeviceId);
            this.handleSlaveMoves(comServerDAO, oldSlavesBySerialNumber, actualSlavesByDeviceId);
        }
    }

    private boolean isVerifyTopologyAction() {
        return this.deviceTopology.getTopologyAction().equals(TopologyAction.VERIFY);
    }

    private boolean isUpdateTopologyAction() {
        return this.deviceTopology.getTopologyAction().equals(TopologyAction.UPDATE);
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

    private void handleSlaveMoves(ComServerDAO comServerDAO, Map<String, OfflineDevice> oldSlavesBySerialNumber, Map<String, DeviceIdentifier> actualSlavesByDeviceId) {
        Set<String> actualSerialNumbers = new HashSet<>(actualSlavesByDeviceId.keySet());
        actualSerialNumbers.removeAll(oldSlavesBySerialNumber.keySet());
        for (String actualSerialNumber : actualSerialNumbers) {
            DeviceIdentifier movedSlave = actualSlavesByDeviceId.get(actualSerialNumber);
            try {
                this.handleMoveOfSlave(comServerDAO, movedSlave);
                this.knownSerialNumbersAddedToTopology.add(actualSerialNumber);
                this.topologyChanged = true;
            } catch (CanNotFindForIdentifier e) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, e.getMessageSeed(), movedSlave));
            }
        }
    }

    private void processJoinedSlaves(ComServerDAO comServerDAO) {
        for (DeviceIdentifier slaveId : this.deviceTopology.getJoinedSlaveDeviceIdentifiers().keySet()) {
            Optional<com.energyict.mdc.protocol.api.device.offline.OfflineDevice> slave = Optional.empty();
            try {
                slave = comServerDAO.findOfflineDevice(slaveId, new DeviceOfflineFlags(SLAVE_DEVICES_FLAG));
            } catch (CanNotFindForIdentifier e) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, e.getMessageSeed(), slaveId));
            }
            if (slave.isPresent()) {
                this.knownSerialNumbersAddedToTopology.add(slave.get().getSerialNumber());
                this.handleMoveOfSlave(comServerDAO, slaveId);
                this.topologyChanged = true;
            } else {
                this.handleAdditionOfSlave(comServerDAO, slaveId);
                this.unknownSerialNumbersAddedToTopology.add(slaveId.toString());
                this.topologyChanged = true;
            }
        }
    }

    private void handleSlaveRemoval(ComServerDAO comServerDAO, Map<String, OfflineDevice> oldSlavesBySerialNumber, Map<String, DeviceIdentifier> actualSlavesByDeviceId) {
        Set<String> oldSerialNumbers = new HashSet<>(oldSlavesBySerialNumber.keySet());
        oldSerialNumbers.removeAll(actualSlavesByDeviceId.keySet());
        for (String oldSerialNumber : oldSerialNumbers) {
            OfflineDevice offlineRemovedSlave = oldSlavesBySerialNumber.get(oldSerialNumber);
            try {
                this.handleRemovalOfSlave(comServerDAO, offlineRemovedSlave);
                this.serialNumbersRemovedFromTopology.add(oldSerialNumber);
                this.topologyChanged = true;
            } catch (CanNotFindForIdentifier e) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, e.getMessageSeed(), offlineRemovedSlave.getDeviceIdentifier()));
            }
        }
    }

    private Map<String, DeviceIdentifier> mapActualSlavedToDeviceIdAndHandleUnknownDevices(ComServerDAO comServerDAO) {
        //TODO port EISERVERSG-4445 from 9.1 code
        Map<String, DeviceIdentifier> actualSlavesByDeviceId = new HashMap<>();
        Collection<DeviceIdentifier> actualSlaveDevices = deviceTopology.getSlaveDeviceIdentifiers().keySet();
        for (DeviceIdentifier slaveId : actualSlaveDevices) {
            Optional<com.energyict.mdc.protocol.api.device.offline.OfflineDevice> slave = Optional.empty();
            try {
                slave = comServerDAO.findOfflineDevice(slaveId, new DeviceOfflineFlags(SLAVE_DEVICES_FLAG));
            } catch (CanNotFindForIdentifier e) {
                this.addIssue(
                        CompletionCode.ConfigurationWarning,
                        getIssueService().newProblem(deviceTopology, e.getMessageSeed(), slaveId));
            }
            if (slave.isPresent()) {
                actualSlavesByDeviceId.put(slave.get().getSerialNumber(), slaveId);
            } else {
                this.handleAdditionOfSlave(comServerDAO, slaveId);
                this.unknownSerialNumbersAddedToTopology.add(slaveId.toString());
                this.topologyChanged = true;
            }
        }
        return actualSlavesByDeviceId;
    }

    private Map<String, OfflineDevice> mapOldSlavesToSerialNumber(OfflineDevice device) {
        Map<String, OfflineDevice> oldSlavesBySerialNumber = new HashMap<>();
        List<? extends OfflineDevice> oldSlaveDevices = device.getAllSlaveDevices();
        for (OfflineDevice slave : oldSlaveDevices) {
            oldSlavesBySerialNumber.put(slave.getSerialNumber(), slave);
        }
        return oldSlavesBySerialNumber;
    }

    private Map<String, DeviceIdentifier> mapRemovedSlavesToSerialNumber(ComServerDAO comServerDAO) {
        Map<String, DeviceIdentifier> removedSlavesBySerialNumber = new HashMap<>();
        Collection<DeviceIdentifier> lostSlaveDevices = deviceTopology.getLostSlaveDeviceIdentifiers();
        if (lostSlaveDevices != null) {
            for (DeviceIdentifier slaveId : lostSlaveDevices) {
                Optional<com.energyict.mdc.protocol.api.device.offline.OfflineDevice> slave = Optional.empty();
                try {
                    slave = comServerDAO.findOfflineDevice(slaveId, new DeviceOfflineFlags(SLAVE_DEVICES_FLAG));
                } catch (CanNotFindForIdentifier e) {
                    this.addIssue(
                            CompletionCode.ConfigurationWarning,
                            getIssueService().newProblem(deviceTopology, e.getMessageSeed(), slaveId));
                }
                if (slave.isPresent()) {
                    removedSlavesBySerialNumber.put(slave.get().getSerialNumber(), slaveId);
                }
            }
        }
        return removedSlavesBySerialNumber;
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
            comServerDAO.updateGateway(removedSlave.getDeviceIdentifier(), null);
        }
    }

    /**
     * Method to execute when we detect a new slave device has been added to the device.
     * In this case a new slave device - unknown to EIServer - has been added to the device.
     *
     * @param comServerDAO the ComServerDAO to be used
     * @param addedSlave the new slave that has been added to the device
     */
    private void handleAdditionOfSlave(ComServerDAO comServerDAO, DeviceIdentifier addedSlave) {
        UnknownSlaveDeviceEvent event = new UnknownSlaveDeviceEvent(this.offlineMasterDevice.getmRID(), addedSlave);
        comServerDAO.signalEvent(EventType.UNKNOWN_SLAVE_DEVICE.topic(), event);
    }

    /**
     * Method to execute when we detect an existing slave device being moved to another master.
     * In this case in the real world the slave device has been moved to a new master, but EIServer is not yet aware of this move.
     *
     * @param comServerDAO the ComServerDAO to be used
     * @param movedSlave the slave that is moved and for which we should update its gateway
     */
    private void handleMoveOfSlave(ComServerDAO comServerDAO, DeviceIdentifier movedSlave) {
        if (deviceTopology.getTopologyAction() == TopologyAction.UPDATE) {
            comServerDAO.updateGateway(movedSlave, deviceTopology.getDeviceIdentifier());
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addLabel(this.deviceTopology.getTopologyAction().toString());
            builder.addProperty("deviceIdentifier").append(this.deviceTopology.getDeviceIdentifier());
        }
    }

    protected Optional<CollectedDeviceTopologyEvent> newEvent(List<Issue> issues) {
        CollectedDeviceTopologyEvent event = new CollectedDeviceTopologyEvent(new ComServerEventServiceProvider(), deviceTopology);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}