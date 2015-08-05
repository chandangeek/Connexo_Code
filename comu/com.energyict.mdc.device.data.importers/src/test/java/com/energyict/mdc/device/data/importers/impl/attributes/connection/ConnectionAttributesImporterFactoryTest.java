package com.energyict.mdc.device.data.importers.impl.attributes.connection;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.NUMBER_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat.FORMAT3;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionAttributesImporterFactoryTest {

    private DeviceDataImporterContext context;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Logger logger;

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceService);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            for (TranslationKey translation : TranslationKeys.values()) {
                if (translation.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return translation.getDefaultFormat();
                }
            }
            return invocationOnMock.getArguments()[1];
        });
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createConnectionAttributesImporter() {
        ConnectionAttributesImportFactory factory = new ConnectionAttributesImportFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatInfo(FORMAT3));
        return factory.createImporter(properties);
    }

    @Test
    public void testMandatoryColumnsMissed() {
        String csv = "Device MRID;Connection method name\n" +
                ";";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createConnectionAttributesImporter();

        importer.process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.FILE_FORMAT_ERROR.getTranslated(thesaurus, 2, 2, 0));
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
    }

    @Test
    public void testNoSuchDevice() {
        String csv = "Device MRID;Connection method name\n" +
                "VPB0001;Outbound TCP";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_DEVICE.getTranslated(thesaurus, 2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testNoSuchConnectionMethodEvenOnConfiguration() {
        String csv = "Device MRID;Connection method name\n" +
                "VPB0001;Outbound TCP";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mockDevice("VPB0001");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_CONNECTION_METHOD_ON_DEVICE.getTranslated(thesaurus, 2, "Outbound TCP"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testConnectionMethodNameUniquenessThroughFile() {
        String csv = "Device MRID;Connection method name\n" +
                "VPB0001;Outbound TCP1\n" +
                "VPB0002;Outbound TCP2\n" +
                "VPB0003;Outbound TCP1\n";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        Device device = mockDevice("VPB0001");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        mockDevice("VPB0002");
        mockDevice("VPB0003");

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.NO_CONNECTION_METHOD_ON_DEVICE.getTranslated(thesaurus, 2, "Outbound TCP1"));
        verify(logger).severe(MessageSeeds.CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE.getTranslated(thesaurus, 3));
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    public void testSetAttributesButMandatoryOnesMissed() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;;;";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ConnectionTask connectionTask = mockConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        when(typedProperties.hasValueFor("attr1")).thenReturn(false);
        when(typedProperties.hasValueFor("attr2")).thenReturn(false);
        when(typedProperties.hasValueFor("attr3")).thenReturn(false);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger).info(MessageSeeds.REQUIRED_CONNECTION_ATTRIBUTES_MISSED.getTranslated(thesaurus, 2, "attr1, attr2"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));
    }

    @Test
    public void testSetAttributesSuccessfully() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;100.25;false";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ConnectionTask connectionTask = mockConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));

        verify(connectionTask).setProperty("attr1", "string");
        verify(connectionTask).setProperty("attr2", new BigDecimal("100.25"));
        verify(connectionTask).setProperty("attr3", false);
        verify(connectionTask).save();
    }

    @Test
    public void testUnableToParseAttributes() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;string;string";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        ConnectionTask connectionTask = mockConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.LINE_FORMAT_ERROR.getTranslated(thesaurus, 2, "attr2", "123456789.012"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));

        verify(connectionTask).setProperty("attr1", "string");
        verify(connectionTask, never()).save();
    }

    @Test
    public void testCreateNewConnectionMethodOutbound() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;100.25;true";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        PartialOutboundConnectionTask partialConnectionTask = (PartialOutboundConnectionTask) mockPartialConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));
        Device.ScheduledConnectionTaskBuilder builder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(device.getScheduledConnectionTaskBuilder(partialConnectionTask)).thenReturn(builder);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)).thenReturn(builder);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));

        verify(builder).add();
        verify(builder).setProperty("attr1", "string");
        verify(builder).setProperty("attr2", new BigDecimal("100.25"));
        verify(builder).setProperty("attr3", true);
    }

    @Test
    public void testCreateNewConnectionMethodOutboundIncomplete() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;100.25;1";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        PartialOutboundConnectionTask partialConnectionTask = (PartialOutboundConnectionTask) mockPartialConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));
        Device.ScheduledConnectionTaskBuilder builder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(device.getScheduledConnectionTaskBuilder(partialConnectionTask)).thenReturn(builder);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)).thenThrow(RuntimeException.class);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)).thenReturn(builder);
        ScheduledConnectionTask connectionTask = (ScheduledConnectionTask)mockConnectionTaskWithProperties(false, "Outbound TCP", propertySpecs);
        when(builder.add()).thenReturn(connectionTask);
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        when(typedProperties.hasValueFor("attr1")).thenReturn(false);
        when(typedProperties.hasValueFor("attr2")).thenReturn(false);
        when(typedProperties.hasValueFor("attr3")).thenReturn(false);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger).info(MessageSeeds.REQUIRED_CONNECTION_ATTRIBUTES_MISSED.getTranslated(thesaurus, 2, "attr1, attr2"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));

        verify(builder).add();
    }

    @Test
    public void testCreateNewConnectionMethodInbound() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;100.25;1";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        PartialInboundConnectionTask partialConnectionTask = (PartialInboundConnectionTask) mockPartialConnectionTaskWithProperties(true, "Outbound TCP", propertySpecs);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));
        Device.InboundConnectionTaskBuilder builder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(partialConnectionTask)).thenReturn(builder);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)).thenReturn(builder);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));

        verify(builder).add();
    }

    @Test
    public void testCreateNewConnectionMethodInboundIncomplete() {
        String csv = "Device MRID;Connection method name;attr1;attr2;attr3\n" +
                "VPB0001;Outbound TCP;string;100.25;1";

        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        Device device = mockDevice("VPB0001");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<PropertySpec> propertySpecs = Arrays.asList(
                mockPropertySpec("attr1", new StringFactory(), true),
                mockPropertySpec("attr2", new BigDecimalFactory(), true),
                mockPropertySpec("attr3", new BooleanFactory(), false));
        PartialInboundConnectionTask partialConnectionTask = (PartialInboundConnectionTask) mockPartialConnectionTaskWithProperties(true, "Outbound TCP", propertySpecs);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));
        Device.InboundConnectionTaskBuilder builder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(partialConnectionTask)).thenReturn(builder);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)).thenThrow(RuntimeException.class);
        when(builder.setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)).thenReturn(builder);
        InboundConnectionTask connectionTask = (InboundConnectionTask)mockConnectionTaskWithProperties(true, "Outbound TCP", propertySpecs);
        when(builder.add()).thenReturn(connectionTask);
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        when(typedProperties.hasValueFor("attr1")).thenReturn(false);
        when(typedProperties.hasValueFor("attr2")).thenReturn(false);
        when(typedProperties.hasValueFor("attr3")).thenReturn(false);

        createConnectionAttributesImporter().process(importOccurrence);

        verify(logger).info(MessageSeeds.REQUIRED_CONNECTION_ATTRIBUTES_MISSED.getTranslated(thesaurus, 2, "attr1, attr2"));
        verify(logger, never()).severe(Matchers.anyString());
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));

        verify(builder).add();
    }

    private Device mockDevice(String mRID) {
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn(mRID);
        when(deviceService.findByUniqueMrid(mRID)).thenReturn(Optional.of(device));
        return device;
    }

    private ConnectionTask mockConnectionTaskWithProperties(boolean inbound, String name, List<PropertySpec> propertySpecs) {
        ConnectionTask connectionTask = inbound ? mock(InboundConnectionTask.class) : mock(ScheduledConnectionTask.class);
        when(connectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        for (PropertySpec propertySpec : propertySpecs) {
            String key = propertySpec.getName();
            when(connectionType.getPropertySpec(key)).thenReturn(propertySpec);
        }
        when(connectionType.getPropertySpecs()).thenReturn(propertySpecs);
        TypedProperties typedProperties = mock(TypedProperties.class);
        when(connectionTask.getTypedProperties()).thenReturn(typedProperties);
        return connectionTask;
    }

    private PartialConnectionTask mockPartialConnectionTaskWithProperties(boolean inbound, String name, List<PropertySpec> propertySpecs) {
        PartialConnectionTask connectionTask = inbound ? mock(PartialInboundConnectionTask.class) : mock(PartialOutboundConnectionTask.class);
        when(connectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        for (PropertySpec propertySpec : propertySpecs) {
            String key = propertySpec.getName();
            when(connectionType.getPropertySpec(key)).thenReturn(propertySpec);
        }
        when(connectionType.getPropertySpecs()).thenReturn(propertySpecs);
        TypedProperties typedProperties = mock(TypedProperties.class);
        when(connectionTask.getTypedProperties()).thenReturn(typedProperties);
        return connectionTask;
    }

    private PropertySpec mockPropertySpec(String key, ValueFactory valueFactory, boolean required) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(key);
        when(propertySpec.isRequired()).thenReturn(required);
        doReturn(valueFactory).when(propertySpec).getValueFactory();
        return propertySpec;
    }
}
