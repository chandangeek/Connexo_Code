package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Straightforward implementation of a DeviceConfigConflictMapping
 */
public class DeviceConfigConflictMappingImpl implements DeviceConfigConflictMapping {

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

    @Inject
    public DeviceConfigConflictMappingImpl() {
    }

    public DeviceConfigConflictMapping initialize(DeviceTypeImpl deviceType, DeviceConfiguration origin, DeviceConfiguration destination) {
        this.deviceType.set(deviceType);
        this.originDeviceConfig.set(origin);
        this.destinationDeviceConfig.set(destination);
        return this;
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
}
