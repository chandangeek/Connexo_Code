/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.device;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "DeviceProcessAssociationProvider",
        service = {ProcessAssociationProvider.class, TranslationKeyProvider.class},
        property = "name=DeviceProcessAssociationProvider", immediate = true)
public class DeviceProcessAssociationProvider implements ProcessAssociationProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "CBP";
    public static final String ASSOCIATION_TYPE = "device";

    private volatile License license;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    //For OSGI purposes
    public DeviceProcessAssociationProvider() {
    }

    //For testing purposes
    @Inject
    public DeviceProcessAssociationProvider(Thesaurus thesaurus, PropertySpecService propertySpecService, FiniteStateMachineService finiteStateMachineService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.DEVICE_ASSOCIATION_PROVIDER).format();
    }

    @Override
    public String getType() {
        return ASSOCIATION_TYPE;
    }

    @Override
    public String getAppKey() {
        return APP_KEY;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getDeviceStatePropertySpec());
        return builder.build();
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return (TranslationKeys.DEVICE_STATE_TITLE.getKey()
                .equals(name)) ? Optional.of(getDeviceStatePropertySpec()) : Optional.empty();
    }

    private PropertySpec getDeviceStatePropertySpec() {
        DeviceStateInfo[] possibleValues =
                this.deviceLifeCycleConfigurationService
                        .findAllDeviceLifeCycles().stream()
                        .flatMap(lifeCycle -> lifeCycle.getFiniteStateMachine().getStates().stream())
                        .map(state -> new DeviceStateInfo(deviceLifeCycleConfigurationService, state))
                        .sorted((info1, info2) -> (info1.getLifeCycleId() != info2.getLifeCycleId()) ?
                                info1.getLifeCycleName().compareToIgnoreCase(info2.getLifeCycleName()) :
                                info1.getName().compareToIgnoreCase(info2.getName()))
                        .toArray(DeviceStateInfo[]::new);

        return this.propertySpecService
                .specForValuesOf(new DeviceStateInfoValuePropertyFactory())
                .named(TranslationKeys.DEVICE_STATE_TITLE.getKey(), TranslationKeys.DEVICE_STATE_TITLE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .markMultiValued(",")
                .addValues(possibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish();
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @XmlRootElement
    static class DeviceStateInfo extends HasIdAndName {
        private transient DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
        private transient DeviceLifeCycle deviceLifeCycle;
        private transient State deviceState;

        DeviceStateInfo(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, State deviceState) {
            this.deviceState = deviceState;
            this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
            this.deviceLifeCycle = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()
                    .stream()
                    .filter(lifeCycle -> lifeCycle.getFiniteStateMachine().equals(deviceState.getFiniteStateMachine()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Long getId() {
            return deviceState.getId();
        }

        @Override
        public String getName() {
            return DefaultState
                    .from(deviceState)
                    .map(deviceLifeCycleConfigurationService::getDisplayName)
                    .orElseGet(deviceState::getName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DeviceStateInfo that = (DeviceStateInfo) o;

            return deviceState.getId() == that.deviceState.getId();

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(deviceState.getId());
            return result;
        }

        public Long getLifeCycleId() {
            return (deviceLifeCycle != null) ? deviceLifeCycle.getId() : null;
        }

        public String getLifeCycleName() {
            return (deviceLifeCycle != null) ? deviceLifeCycle.getName() : null;
        }
    }

    private class DeviceStateInfoValuePropertyFactory implements ValueFactory<HasIdAndName>, BpmProcessPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return finiteStateMachineService
                    .findFiniteStateById(Long.parseLong(stringValue))
                    .map(state -> new DeviceStateInfo(deviceLifeCycleConfigurationService, state))
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            }
            else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            }
            else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
