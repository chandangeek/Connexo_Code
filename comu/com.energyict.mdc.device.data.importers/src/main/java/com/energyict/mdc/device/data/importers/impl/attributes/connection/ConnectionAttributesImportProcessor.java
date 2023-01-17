/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter.PropertiesConverterConfig;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.upl.TypedProperties;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConnectionAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<ConnectionAttributesImportRecord> {

    private final PropertiesConverterConfig propertiesConverterConfig;

    private boolean isFirstRow = true;
    private String connectionMethod;
    private final SecurityManagementService securityManagementService;

    ConnectionAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat, SecurityManagementService securityManagementService) {
        super(context);
        this.propertiesConverterConfig = PropertiesConverterConfig.newConfig().withNumberFormat(numberFormat);
        this.securityManagementService = securityManagementService;
    }

    @Override
    public void process(ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        validateConnectionMethodUniquenessInFile(data);
        Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream()
                .filter(task -> task.getName().equals(data.getConnectionMethod())).findAny();
        if (connectionTask.isPresent()) {
            validateConnectionMethodProperties(data, connectionTask.get().getConnectionType(), logger);
            ConnectionTask task = connectionTask.get();
            setConnectionAttributes(data, task.getConnectionType(), task::setProperty, new KeyAccessorSetter(device, task)::setKeyAccessor);
            task.saveAllProperties();
            logMissingPropertiesIfIncomplete(task, data, logger);
        } else {
            PartialConnectionTask partialConnectionTask = device.getDeviceConfiguration().getPartialConnectionTasks()
                    .stream().filter(task -> task.getName().equals(data.getConnectionMethod()))
                    .findAny()
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
            setConnectionAttributes(data, partialConnectionTask.getConnectionType(), inboundConnectionTaskBuilder::setProperty, new KeyAccessorSetter(device, partialConnectionTask)::setKeyAccessor);
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
        setConnectionAttributes(data, partialConnectionTask.getConnectionType(), scheduledConnectionTaskBuilder::setProperty, new KeyAccessorSetter(device, partialConnectionTask)::setKeyAccessor);
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

    private void setConnectionAttributes(ConnectionAttributesImportRecord data, ConnectionType connectionType, BiConsumer<String, Object> propertySetter, BiConsumer<String, Object> keyAccessorSetter) {
        connectionType.getPropertySpecs().stream()
                .filter(propertySpec -> data.getConnectionAttributes().containsKey(propertySpec.getName()))
                .forEach(propertySpec -> {
                    String name = propertySpec.getName();
                    String value = data.getConnectionAttributes().get(name);
                    if (propertySpec.isReference() && (SecurityAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))) {
                        keyAccessorSetter.accept(name, value);
                    } else {
                        propertySetter.accept(name, parseStringToValue(propertySpec, value, data));
                    }
                });
    }

    /**
     * This class allows setting the KeyAccessor's actual value to the provided value.
     * Both KeyAccessor and actual value are created if absent on device level.
     * Supports only plaintext symmetric keys and plaintext passphrases
     */
    private class KeyAccessorSetter {
        private final Device device;
        private final Function<String, SecurityAccessorType> keyAccessorTypeGetter;

        KeyAccessorSetter(Device device, ConnectionTask task) {
            this.device = device;
            this.keyAccessorTypeGetter = name -> (SecurityAccessorType) task.getProperty(name).getValue();
        }

        KeyAccessorSetter(Device device, PartialConnectionTask partialConnectionTask) {
            this.device = device;
            this.keyAccessorTypeGetter = name -> (SecurityAccessorType) partialConnectionTask.getProperty(name).getValue();
        }

        void setKeyAccessor(String name, Object value) {
            SecurityAccessorType securityAccessorType = keyAccessorTypeGetter.apply(name);
            securityManagementService.getDeviceSecretImporter(securityAccessorType); // to verify that security accessor type is supported by import
            // TODO: try refactoring further code with DeviceSecretImporter
            SecurityAccessor<SecurityValueWrapper> securityAccessor = device.getSecurityAccessor(securityAccessorType)
                    .orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
            SecurityValueWrapper securityValueWrapper = securityAccessor.getActualValue()
                    .orElse(isKey(securityAccessorType) ?
                            securityManagementService.newSymmetricKeyWrapper(securityAccessorType) :
                            securityManagementService.newPassphraseWrapper(securityAccessorType));
            securityAccessor.setActualValue(securityValueWrapper);
            Map<String, Object> properties = new HashMap<>();
            properties.put(isKey(securityAccessorType)?"key":"passphrase", value);
            securityValueWrapper.setProperties(properties);
            securityAccessor.save();
        }

        private boolean isKey(SecurityAccessorType securityAccessorType) {
            return securityAccessorType.getKeyType().getCryptographicType().equals(CryptographicType.SymmetricKey) || securityAccessorType.getKeyType().getCryptographicType().equals(CryptographicType.Hsm);
        }
    }

}
