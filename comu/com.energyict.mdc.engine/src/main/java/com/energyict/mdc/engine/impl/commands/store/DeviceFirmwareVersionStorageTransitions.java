package com.energyict.mdc.engine.impl.commands.store;

/**
 * Keeps track of the transitions of each FirmwareVersion
 */
public class DeviceFirmwareVersionStorageTransitions {

    private FirmwareVersionStorageTransition activeMeterFirmwareVersionTransition = FirmwareVersionStorageTransition.UNKNOWN;
    private FirmwareVersionStorageTransition passiveMeterFirmwareVersionTransition = FirmwareVersionStorageTransition.UNKNOWN;
    private FirmwareVersionStorageTransition activeCommunicationFirmwareVersionTransition = FirmwareVersionStorageTransition.UNKNOWN;
    private FirmwareVersionStorageTransition passiveCommunicationFirmwareVersionTransition = FirmwareVersionStorageTransition.UNKNOWN;

    public FirmwareVersionStorageTransition getActiveMeterFirmwareVersionTransition() {
        return activeMeterFirmwareVersionTransition;
    }

    public void setActiveMeterFirmwareVersionTransition(FirmwareVersionStorageTransition activeMeterFirmwareVersionTransition) {
        this.activeMeterFirmwareVersionTransition = activeMeterFirmwareVersionTransition;
    }

    public FirmwareVersionStorageTransition getPassiveMeterFirmwareVersionTransition() {
        return passiveMeterFirmwareVersionTransition;
    }

    public void setPassiveMeterFirmwareVersionTransition(FirmwareVersionStorageTransition passiveMeterFirmwareVersionTransition) {
        this.passiveMeterFirmwareVersionTransition = passiveMeterFirmwareVersionTransition;
    }

    public FirmwareVersionStorageTransition getActiveCommunicationFirmwareVersionTransition() {
        return activeCommunicationFirmwareVersionTransition;
    }

    public void setActiveCommunicationFirmwareVersionTransition(FirmwareVersionStorageTransition activeCommunicationFirmwareVersionTransition) {
        this.activeCommunicationFirmwareVersionTransition = activeCommunicationFirmwareVersionTransition;
    }

    public FirmwareVersionStorageTransition getPassiveCommunicationFirmwareVersionTransition() {
        return passiveCommunicationFirmwareVersionTransition;
    }

    public void setPassiveCommunicationFirmwareVersionTransition(FirmwareVersionStorageTransition passiveCommunicationFirmwareVersionTransition) {
        this.passiveCommunicationFirmwareVersionTransition = passiveCommunicationFirmwareVersionTransition;
    }

}
