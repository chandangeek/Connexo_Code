/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Straightforward implementation ofa ConflictingSecuritySetSolution
 */
@ActionTypeMapHasDestination(groups = {Save.Create.class, Save.Update.class}, getDestination = ConflictingSecuritySetSolutionImpl.DESTINATION_SECURITY_PROPERTY_SET_FIELD_NAME)
public class ConflictingSecuritySetSolutionImpl extends AbstractConflictSolution<SecurityPropertySet> implements ConflictingSecuritySetSolution {

    /**
     * Created a static field for this so it can be used in the javaxValidation annotation
     */
    static final String DESTINATION_SECURITY_PROPERTY_SET_FIELD_NAME = "destinationSecurityPropertySet";

    public enum Fields {
        CONFLICTINGMAPPING("conflictingMapping"),
        ACTION("action"),
        ORIGINSECURITYSET("originSecurityPropertySet"),
        DESTINATIONSECURITYSET(DESTINATION_SECURITY_PROPERTY_SET_FIELD_NAME);

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }
    private Reference<SecurityPropertySet> originSecurityPropertySet = ValueReference.absent();
    private Reference<SecurityPropertySet> destinationSecurityPropertySet = ValueReference.absent();

    @Inject
    public ConflictingSecuritySetSolutionImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public List<SecurityPropertySet> getMappableToDataSources() {
        return getConflictingMapping().getDestinationDeviceConfiguration().getSecurityPropertySets().stream().filter(securityPropertySet -> securityPropertySet.getAuthenticationDeviceAccessLevel().getId() == getOriginDataSource().getAuthenticationDeviceAccessLevel().getId() && securityPropertySet.getEncryptionDeviceAccessLevel().getId() == getOriginDataSource().getEncryptionDeviceAccessLevel().getId()).collect(Collectors.toList());
    }

    @Override
    Reference<SecurityPropertySet> getOriginDataSourceReference() {
        return originSecurityPropertySet;
    }

    @Override
    Reference<SecurityPropertySet> getDestinationDataSourceReference() {
        return destinationSecurityPropertySet;
    }

    public ConflictingSecuritySetSolution initialize(DeviceConfigConflictMappingImpl deviceConfigConflictMapping, SecurityPropertySet origin) {
        setConflictingMapping(deviceConfigConflictMapping);
        this.originSecurityPropertySet.set(origin);
        this.destinationSecurityPropertySet.setNull();
        this.action = DeviceConfigConflictMapping.ConflictingMappingAction.NOT_DETERMINED_YET;
        return this;
    }

}
