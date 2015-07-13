package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Copyrights EnergyICT
 * Date: 13/07/15
 * Time: 15:12
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

    @Override
    public DeviceConfiguration getOriginDeviceConfiguration() {
        return originDeviceConfig.orElseThrow(originDeviceConfigurationIsRequired());
    }

    private Supplier<? extends RuntimeException> originDeviceConfigurationIsRequired() {
        // TODO
        return null;
    }

    @Override
    public DeviceConfiguration getDestinationDeviceConfiguration() {
        return destinationDeviceConfig.orElseThrow(destinationDeviceConfigurationIsRequired());
    }

    private Supplier<? extends RuntimeException> destinationDeviceConfigurationIsRequired() {
        //TODO
        return null;
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
