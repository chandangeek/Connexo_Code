package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Straightforward implementation of a DeviceConfigConflictMapping
 *
 * TODO validate that there is only one solution per conlfictinMethod or conflictingSecuritySet
 *
 */
public class DeviceConfigConflictMappingImpl implements DeviceConfigConflictMapping{

    private final DataModel dataModel;

    enum Fields {
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

        String fieldName() {
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

    private long id;

    @Inject
    public DeviceConfigConflictMappingImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DeviceConfigConflictMapping initialize(DeviceTypeImpl deviceType, DeviceConfiguration origin, DeviceConfiguration destination) {
        this.deviceType.set(deviceType);
        this.originDeviceConfig.set(origin);
        this.destinationDeviceConfig.set(destination);
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
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void newConflictingConnectionMethods(PartialConnectionTask origin, PartialConnectionTask destination) {
        markAsNotSolved();
        this.connectionMethodSolutions.add(dataModel.getInstance(ConflictingConnectionMethodSolutionImpl.class).initialize(this, origin, destination));
    }

    @Override
    public void newConflictingSecurityPropertySets(SecurityPropertySet origin, SecurityPropertySet destination) {
        markAsNotSolved();
        this.securitySetSolutions.add(dataModel.getInstance(ConflictingSecuritySetSolutionImpl.class).initialize(this, origin, destination));
    }

    @Override
    public void removeConnectionMethodSolution(ConflictingConnectionMethodSolution conflictingConnectionMethodSolution) {
        this.connectionMethodSolutions.remove(conflictingConnectionMethodSolution);
    }

    @Override
    public void removeSecuritySetSolution(ConflictingSecuritySetSolution conflictingSecuritySetSolution) {
        this.securitySetSolutions.remove(conflictingSecuritySetSolution);
    }

    private void markAsNotSolved() {
        this.solved = false;
    }
}
