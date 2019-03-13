package com.energyict.mdc.device.config.properties;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleInDeviceTypePropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.MessageSeeds;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceLifeCycleInDeviceTypeInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceLifeCycleInDeviceTypePropertyFactory {

    protected volatile Thesaurus thesaurus;
    static final String NAME = "BasicDataCollectionRuleTemplate";
    private static final String SEPARATOR = ":";
    public static final String DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES = NAME + ".deviceLifecyleInDeviceTypes";

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    //DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService

    //for OSGI
    public DeviceLifeCycleInDeviceTypeInfoValueFactory() {
    }

    @Inject
    public DeviceLifeCycleInDeviceTypeInfoValueFactory(NlsService nlsService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        setNlsService(nlsService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public HasIdAndName fromStringValue(String stringValue) {
        List<String> values = Arrays.asList(stringValue.split(SEPARATOR));
        if (values.size() != 3) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                    "properties." + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES,
                    String.valueOf(3),
                    String.valueOf(values.size()));
        }
        long deviceTypeId = Long.parseLong(values.get(0));
        DeviceType deviceType = deviceConfigurationService
                .findDeviceType(deviceTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Devicetype with id " + deviceTypeId + " does not exist"));
        if (!(deviceType.getDeviceLifeCycle().getId() == Long.parseLong(values.get(1)))) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                    "properties." + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES,
                    values.get(1));
        }

        List<Long> stateIds = Arrays.stream(values.get(2)
                .split(","))
                .map(String::trim)
                .mapToLong(Long::parseLong).boxed().collect(Collectors.toList());

        List<State> states = deviceLifeCycleConfigurationService
                .findAllDeviceLifeCycles().find()
                .stream().map(lifecycle -> lifecycle.getFiniteStateMachine().getStates())
                .flatMap(Collection::stream)
                .filter(stateValue -> stateIds.contains(stateValue.getId())).collect(Collectors.toList());
        return new DeviceLifeCycleInDeviceTypeInfo(deviceType, states, deviceLifeCycleConfigurationService);
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
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, HasIdAndName value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}