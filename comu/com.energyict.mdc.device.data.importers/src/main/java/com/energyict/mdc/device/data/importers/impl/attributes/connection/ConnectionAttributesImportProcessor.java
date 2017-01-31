/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter.PropertiesConverterConfig;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ConnectionAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<ConnectionAttributesImportRecord> {

    private final PropertiesConverterConfig propertiesConverterConfig;

    private boolean isFirstRow = true;
    private String connectionMethod;

    ConnectionAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat) {
        super(context);
        this.propertiesConverterConfig = PropertiesConverterConfig.newConfig().withNumberFormat(numberFormat);
    }

    @Override
    public void process(ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        validateConnectionMethodUniquenessInFile(data);
        Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream()
                .filter(task -> task.getName().equals(data.getConnectionMethod())).findFirst();
        if (connectionTask.isPresent()) {
            validateConnectionMethodProperties(data, connectionTask.get().getConnectionType(), logger);
            ConnectionTask task = connectionTask.get();
            setConnectionAttributes(data, task.getConnectionType(), task::setProperty);
            task.save();
            logMissingPropertiesIfIncomplete(task, data, logger);
        } else {
            PartialConnectionTask partialConnectionTask = device.getDeviceConfiguration().getPartialConnectionTasks()
                    .stream().filter(task -> task.getName().equals(data.getConnectionMethod()))
                    .findFirst()
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_CONNECTION_METHOD_ON_DEVICE, data.getLineNumber(), data.getConnectionMethod()));
            validateConnectionMethodProperties(data, partialConnectionTask.getConnectionType(), logger);
            createConnectionTaskOnDevice(device, partialConnectionTask, data, logger);
        }
        isFirstRow = false;
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private void logMissingPropertiesIfIncomplete(ConnectionTask<?, ?> connectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) {
        if (connectionTask.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)) {
            TypedProperties properties = connectionTask.getTypedProperties();
            String missingProperties = connectionTask.getConnectionType().getPropertySpecs().stream()
                    .filter(PropertySpec::isRequired)
                    .map(PropertySpec::getName)
                    .filter(propertySpec -> !properties.hasValueFor(propertySpec))
                    .collect(Collectors.joining(", "));
            if (!missingProperties.isEmpty()) {
                logger.warning(MessageSeeds.REQUIRED_CONNECTION_ATTRIBUTES_MISSED, data.getLineNumber(), missingProperties);
            }
        }
    }

    private void validateConnectionMethodUniquenessInFile(ConnectionAttributesImportRecord data) {
        if (connectionMethod == null) {
            connectionMethod = data.getConnectionMethod();
        } else if (!connectionMethod.equals(data.getConnectionMethod())) {
            throw new ProcessorException(MessageSeeds.CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE, data.getLineNumber()).andStopImport();
        }
    }

    private void validateConnectionMethodProperties(ConnectionAttributesImportRecord data, ConnectionType connectionType, FileImportLogger logger) {
        if (isFirstRow) {
            String unknownAttributes = data.getConnectionAttributes().keySet().stream()
                    .filter(key -> connectionType.getPropertySpec(key) == null).collect(Collectors.joining(", "));
            if (!unknownAttributes.isEmpty()) {
                logger.warning(MessageSeeds.UNKNOWN_CONNECTION_ATTRIBUTE, data.getConnectionMethod(), unknownAttributes);
            }
        }
    }

    private Object parseStringToValue(PropertySpec propertySpec, String value, ConnectionAttributesImportRecord data) {
        ValueFactory<?> valueFactory = propertySpec.getValueFactory();
        Optional<DynamicPropertyConverter> propertyParser = DynamicPropertyConverter.of(valueFactory.getClass());
        Object parsedValue;
        try {
            if (propertyParser.isPresent()) {
                value = propertyParser.get().configure(propertiesConverterConfig).convert(value);
            }
            parsedValue = valueFactory.fromStringValue(value);
        } catch (Exception e) {
            String expectedFormat = propertyParser.isPresent() ? propertyParser.get().getExpectedFormat(getContext().getThesaurus()) : valueFactory.getValueType().getName();
            throw new ProcessorException(MessageSeeds.LINE_FORMAT_ERROR, data.getLineNumber(), propertySpec.getName(), expectedFormat);
        }
        try {
            propertySpec.validateValue(parsedValue);
        } catch (InvalidValueException e) {
            throw new ProcessorException(MessageSeeds.CONNECTION_ATTRIBUTE_INVALID_VALUE, data.getLineNumber(), parsedValue, propertySpec.getName());
        }
        return parsedValue;
    }

    private void createConnectionTaskOnDevice(Device device, PartialConnectionTask partialConnectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) {
        if (partialConnectionTask instanceof PartialOutboundConnectionTask) {
            addScheduledConnectionTaskToDevice(device, (PartialOutboundConnectionTask) partialConnectionTask, data, logger);
        } else if (partialConnectionTask instanceof PartialInboundConnectionTask) {
            addInboundConnectionTaskToDevice(device, (PartialInboundConnectionTask) partialConnectionTask, data, logger);
        }
    }

    private void addInboundConnectionTaskToDevice(Device device, PartialInboundConnectionTask partialConnectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialConnectionTask);
        try {
            setConnectionAttributes(data, partialConnectionTask.getConnectionType(), inboundConnectionTaskBuilder::setProperty);
            inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                InboundConnectionTask connectionTask = inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
                logMissingPropertiesIfIncomplete(connectionTask, data, logger);
            } catch (ConstraintViolationException ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_METHOD_NOT_CREATED, data.getLineNumber(), data.getConnectionMethod(), device.getName(), buildConstraintViolationCause(ex));
            } catch (Exception ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_METHOD_NOT_CREATED, data.getLineNumber(), data.getConnectionMethod(), device.getName(), ex.getLocalizedMessage());
            }
        }
    }

    private void addScheduledConnectionTaskToDevice(Device device, PartialOutboundConnectionTask partialConnectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialConnectionTask);
        setConnectionAttributes(data, partialConnectionTask.getConnectionType(), scheduledConnectionTaskBuilder::setProperty);
        try {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                ScheduledConnectionTask connectionTask = scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
                logMissingPropertiesIfIncomplete(connectionTask, data, logger);
            } catch (ConstraintViolationException ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_METHOD_NOT_CREATED, data.getLineNumber(), data.getConnectionMethod(), device.getName(), buildConstraintViolationCause(ex));
            } catch (Exception ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_METHOD_NOT_CREATED, data.getLineNumber(), data.getConnectionMethod(), device.getName(), ex.getLocalizedMessage());
            }
        }
    }

    private String buildConstraintViolationCause(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
    }

    private void setConnectionAttributes(ConnectionAttributesImportRecord data, ConnectionType connectionType, BiConsumer<String, Object> setter) {
        connectionType.getPropertySpecs().stream()
                .filter(propertySpec -> data.getConnectionAttributes().containsKey(propertySpec.getName()))
                .forEach(propertySpec -> {
                    String name = propertySpec.getName();
                    setter.accept(name, parseStringToValue(propertySpec, data.getConnectionAttributes().get(name), data));
                });
    }
}
