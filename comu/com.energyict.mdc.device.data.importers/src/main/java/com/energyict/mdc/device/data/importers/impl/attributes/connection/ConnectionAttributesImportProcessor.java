package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
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
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ConnectionAttributesImportProcessor implements FileImportProcessor<ConnectionAttributesImportRecord> {

    private final DeviceDataImporterContext context;

    ConnectionAttributesImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(ConnectionAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = context.getDeviceService().findByUniqueMrid(data.getDeviceMrid())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMrid()));
        Optional<ConnectionTask<?, ?>> connectionTask = device.getConnectionTasks().stream()
                .filter(task -> task.getName().equals(data.getConnectionMethodName())).findFirst();
        if (connectionTask.isPresent()) {
            Iterator<PropertySpec> propertySpecs = connectionTask.get().getConnectionType().getPropertySpecs().iterator();
            Iterator<String> connectionAttributes = data.getConnectionAttributes().iterator();
            while (connectionAttributes.hasNext() && propertySpecs.hasNext()) {
                PropertySpec propertySpec = propertySpecs.next();
                connectionTask.get().setProperty(propertySpec.getName(), parseStringToValue(propertySpec.getValueFactory(), connectionAttributes.next()));
            }
            connectionTask.get().save();
        } else {
//            PartialConnectionTask partialConnectionTask = device.getDeviceConfiguration().getPartialConnectionTasks()
//                    .stream().filter(task -> task.getName().equals(data.getConnectionMethodName()))
//                    .findFirst()
//                    .orElseThrow(() -> new ProcessorException(ProcessorException.Type.CONNECTIONTYPE_NOT_FOUND, mRID, ""));
//            createConnectionTaskOnDevice(device, partialConnectionTask, data);
        }
    }

    private Object parseStringToValue(ValueFactory<?> valueFactory, String value) {
        Optional<DynamicPropertyParser> propertyParser = DynamicPropertyParser.of(valueFactory.getClass());
        Object parsedValue = null;
        try {
            if (propertyParser.isPresent()) {
                parsedValue = propertyParser.get().parse(value);
            } else {
                parsedValue = valueFactory.fromStringValue(value);
            }
        } catch (ParseException e) {
//            throw new ProcessorException(ProcessorException.Type.UNPARSEABLE_VALUE, e, "", "");
        }
        return parsedValue;
    }

    private void createConnectionTaskOnDevice(Device device, PartialConnectionTask partialConnectionTask, Map<String, String> data) {
        if (partialConnectionTask instanceof PartialOutboundConnectionTask) {
            addScheduledConnectionTaskToDevice(device, data, (PartialOutboundConnectionTask) partialConnectionTask);
        } else if (partialConnectionTask instanceof PartialInboundConnectionTask) {
            addInboundConnectionTaskToDevice(device, data, (PartialInboundConnectionTask) partialConnectionTask);
        }
    }

    private void addInboundConnectionTaskToDevice(Device device, Map<String, String> data, PartialInboundConnectionTask partialConnectionTask) throws ProcessorException {
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialConnectionTask);
        try {
            data.forEach((key, value) -> inboundConnectionTaskBuilder.setProperty(key, parseStringToValue(partialConnectionTask.getConnectionType().getPropertySpec(key).getValueFactory(), value)));
            inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
            } catch (Exception ex) {
//                throw new ProcessorException(ProcessorException.Type.CONNECTION_ATTRIBUTES_NOT_CREATED, ex, device.getmRID(), "");
            }
        }
    }

    private void addScheduledConnectionTaskToDevice(Device device, Map<String, String> data, PartialOutboundConnectionTask partialConnectionTask) throws ProcessorException {
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialConnectionTask);
        data.forEach((key, value) -> scheduledConnectionTaskBuilder.setProperty(key, parseStringToValue(partialConnectionTask.getConnectionType().getPropertySpec(key).getValueFactory(), value)));
        try {
            scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE).add();
        } catch (Exception e) {
            try {
                scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
            } catch (Exception ex) {
//                throw new ProcessorException(ProcessorException.Type.CONNECTION_ATTRIBUTES_NOT_CREATED, ex, device.getmRID(), "");
            }
        }
    }
}
