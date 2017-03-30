/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.EventType;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Straightforward implementation of a DeviceConfigConflictMapping
 */
@OnlyOneSolutionPerDataSource(groups = {Save.Create.class, Save.Update.class})
public class DeviceConfigConflictMappingImpl implements DeviceConfigConflictMapping {

    private final DataModel dataModel;
    private final EventService eventService;

    public enum Fields {
        DEVICETYPE("deviceType"),
        ORIGINDEVICECONFIG("originDeviceConfig"),
        DESTINATIONDEVICECONFIG("destinationDeviceConfig"),
        SOLVED("solved"),
        CONNECTIONMETHODSOLUTIONS("connectionMethodSolutions"),
        SECURITYSETSOLUTIONS("securitySetSolutions")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @IsPresent
    private final Reference<DeviceConfiguration> originDeviceConfig = ValueReference.absent();
    @IsPresent
    private final Reference<DeviceConfiguration> destinationDeviceConfig = ValueReference.absent();
    private boolean solved;
    @Valid
    private List<ConflictingConnectionMethodSolution> connectionMethodSolutions = new ArrayList<>();
    @Valid
    private List<ConflictingSecuritySetSolution> securitySetSolutions = new ArrayList<>();

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    // audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    @Inject
    public DeviceConfigConflictMappingImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public DeviceConfigConflictMappingImpl initialize(DeviceType deviceType, DeviceConfiguration origin, DeviceConfiguration destination) {
        this.deviceType.set(deviceType);
        this.originDeviceConfig.set(origin);
        this.destinationDeviceConfig.set(destination);
        notifyConflictCreatingEvent();
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public DeviceConfiguration getOriginDeviceConfiguration() {
        return originDeviceConfig.get();
    }

    @Override
    public DeviceConfiguration getDestinationDeviceConfiguration() {
        return destinationDeviceConfig.get();
    }

    @Override
    public List<ConflictingConnectionMethodSolution> getConflictingConnectionMethodSolutions() {
        return connectionMethodSolutions;
    }

    @Override
    public List<ConflictingSecuritySetSolution> getConflictingSecuritySetSolutions() {
        return securitySetSolutions;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    void recalculateSolvedState(AbstractConflictSolution conflictingSolution) {
        conflictingSolution.update();
        this.update();
    }

    private void updateSolvedState() {
        this.solved = allConnectionMethodsHaveASolution() && allSecuritySetsHaveASolution();
    }

    private void update() {
        updateSolvedState();
        Save.UPDATE.save(dataModel, this);
        dataModel.touch(deviceType.get());
    }

    private boolean allSecuritySetsHaveASolution() {
        return this.securitySetSolutions.stream().filter(securitySetSolutionNotDeterminedYet().negate()).count() == securitySetSolutions.size();
    }

    private Predicate<ConflictingSecuritySetSolution> securitySetSolutionNotDeterminedYet() {
        return conflictingSecuritySetSolution -> conflictingSecuritySetSolution.getConflictingMappingAction().equals(ConflictingMappingAction.NOT_DETERMINED_YET);
    }

    private boolean allConnectionMethodsHaveASolution() {
        return this.connectionMethodSolutions.stream().filter(connectionMethodSolutionNotDetermined().negate()).count() == connectionMethodSolutions.size();
    }

    private Predicate<ConflictingConnectionMethodSolution> connectionMethodSolutionNotDetermined() {
        return conflictingConnectionMethodSolution -> conflictingConnectionMethodSolution.getConflictingMappingAction().equals(ConflictingMappingAction.NOT_DETERMINED_YET);
    }

    ConflictingConnectionMethodSolution newConflictingConnectionMethods(PartialConnectionTask origin) {
        markAsNotSolved();
        ConflictingConnectionMethodSolution connectionMethodSolution = dataModel.getInstance(ConflictingConnectionMethodSolutionImpl.class).initialize(this, origin);
        this.connectionMethodSolutions.add(connectionMethodSolution);
        updateWithLockCheck();
        return connectionMethodSolution;
    }

    ConflictingSecuritySetSolution newConflictingSecurityPropertySets(SecurityPropertySet origin) {
        markAsNotSolved();
        ConflictingSecuritySetSolution securitySetSolution = dataModel.getInstance(ConflictingSecuritySetSolutionImpl.class).initialize(this, origin);
        this.securitySetSolutions.add(securitySetSolution);
        updateWithLockCheck();
        return securitySetSolution;
    }

    public void removeConnectionMethodSolution(ConflictingConnectionMethodSolution conflictingConnectionMethodSolution) {
        this.connectionMethodSolutions.remove(conflictingConnectionMethodSolution);
        update();
    }

    public void removeSecuritySetSolution(ConflictingSecuritySetSolution conflictingSecuritySetSolution) {
        this.securitySetSolutions.remove(conflictingSecuritySetSolution);
        update();
    }

    private void markAsNotSolved() {
        this.solved = false;
    }

    private void updateWithLockCheck(){
        notifyConflictCreatingEvent();
        update();
    }

    private void notifyConflictCreatingEvent() {
        eventService.postEvent(EventType.DEVICE_CONFIG_CONFLICT_VALIDATE_CREATE.topic(), this);
    }
}
