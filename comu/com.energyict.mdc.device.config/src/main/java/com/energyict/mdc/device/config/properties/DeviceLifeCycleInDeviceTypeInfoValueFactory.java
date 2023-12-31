package com.energyict.mdc.device.config.properties;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleInDeviceTypePropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceLifeCycleInDeviceTypeInfoValueFactory implements ValueFactory<HasIdAndName>, DeviceLifeCycleInDeviceTypePropertyFactory {

    protected volatile Thesaurus thesaurus;

    private static final String SEPARATOR = ":";
    static final String NAME = "DeviceLifeCycleInDeviceType";
    public static final String DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES = NAME + ".deviceLifecyleInDeviceTypes";

    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringTranslationService meteringTranslationService;

    public DeviceLifeCycleInDeviceTypeInfoValueFactory(DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, MeteringTranslationService meteringTranslationService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.meteringTranslationService = meteringTranslationService;
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
        return new DeviceLifeCycleInDeviceTypeInfo(deviceType, states, meteringTranslationService);
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