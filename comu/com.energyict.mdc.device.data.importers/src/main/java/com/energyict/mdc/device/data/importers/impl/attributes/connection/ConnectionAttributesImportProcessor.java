package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.importers.impl.parsers.DynamicPropertyParser;
import com.energyict.mdc.device.data.importers.impl.parsers.DynamicPropertyParser.PropertiesParserConfig;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectionAttributesImportProcessor implements FileImportProcessor<ConnectionAttributesImportRecord> {

    private final DeviceDataImporterContext context;
    private final PropertiesParserConfig propertiesParserConfig;

    private String connectionMethod;

    ConnectionAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat) {
        this.context = context;
        this.propertiesParserConfig = PropertiesParserConfig.newConfig().withNumberFormat(numberFormat);
    }

    @Override
    public void process(ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = context.getDeviceService().findByUniqueMrid(data.getDeviceMRID())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMRID()));
        validateConnectionMethodUniquenessInFile(data);
        Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream()
                .filter(task -> task.getName().equals(data.getConnectionMethod())).findFirst();
        if (connectionTask.isPresent()) {
            ConnectionTask task = connectionTask.get();
            data.getConnectionAttributes().forEach((key, value) -> {
                task.setProperty(key, parseStringToValue(task.getConnectionType().getPropertySpec(key), value, data));
            });
            task.save();
            checkConnectionTaskStatus(task, data, logger);
        } else {
            PartialConnectionTask partialConnectionTask = device.getDeviceConfiguration().getPartialConnectionTasks()
                    .stream().filter(task -> task.getName().equals(data.getConnectionMethod()))
                    .findFirst()
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_CONNECTION_METHOD_ON_DEVICE, data.getLineNumber(), data.getConnectionMethod()));
            createConnectionTaskOnDevice(device, partialConnectionTask, data, logger);
        }
    }

    private void checkConnectionTaskStatus(ConnectionTask<?, ?> connectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) {
        if (connectionTask.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)) {
            TypedProperties properties = connectionTask.getTypedProperties();
            String missedRequiredProperties = connectionTask.getConnectionType().getPropertySpecs().stream()
                    .filter(PropertySpec::isRequired)
                    .map(PropertySpec::getName)
                    .filter(propertySpec -> !properties.hasValueFor(propertySpec))
                    .collect(Collectors.joining(", "));
            if (!missedRequiredProperties.isEmpty()) {
                logger.warning(MessageSeeds.REQUIRED_CONNECTION_ATTRIBUTES_MISSED, data.getLineNumber(), missedRequiredProperties);
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

    private Object parseStringToValue(PropertySpec propertySpec, String value, ConnectionAttributesImportRecord data) {
        ValueFactory<?> valueFactory = propertySpec.getValueFactory();
        Optional<DynamicPropertyParser> propertyParser = DynamicPropertyParser.of(valueFactory.getClass());
        Object parsedValue;
        try {
            if (propertyParser.isPresent()) {
                parsedValue = propertyParser.get().configure(propertiesParserConfig).parse(value);
            } else {
                parsedValue = valueFactory.fromStringValue(value);
            }
        } catch (Exception e) {
            String expectedFormat = propertyParser.isPresent() ? propertyParser.get().getExpectedFormat(context.getThesaurus()) : valueFactory.getValueType().getName();
            throw new ProcessorException(MessageSeeds.LINE_FORMAT_ERROR, data.getLineNumber(), propertySpec.getName(), expectedFormat);
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
            data.getConnectionAttributes().forEach((key, value) -> inboundConnectionTaskBuilder.setProperty(key, parseStringToValue(partialConnectionTask.getConnectionType().getPropertySpec(key), value, data)));
            inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                InboundConnectionTask connectionTask = inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
                checkConnectionTaskStatus(connectionTask, data, logger);
            } catch (Exception ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_ATTRIBUTES_NOT_CREATED, data.getLineNumber(), device.getmRID());
            }
        }
    }

    private void addScheduledConnectionTaskToDevice(Device device, PartialOutboundConnectionTask partialConnectionTask, ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialConnectionTask);
        data.getConnectionAttributes().forEach((key, value) -> scheduledConnectionTaskBuilder.setProperty(key, parseStringToValue(partialConnectionTask.getConnectionType().getPropertySpec(key), value, data)));
        try {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                ScheduledConnectionTask connectionTask = scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
                checkConnectionTaskStatus(connectionTask, data, logger);
            } catch (Exception ex) {
                throw new ProcessorException(MessageSeeds.CONNECTION_ATTRIBUTES_NOT_CREATED, data.getLineNumber(), device.getmRID());
            }
        }
    }
}
