package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityKeyInfo;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Attribute;
import ch.iec.tc57._2011.meterconfig.ConnectionAttributes;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceBuilderTest {

    private static final Instant NOW = Instant.now();
    private static final Instant SHIPMENT_DATE = Instant.now().minus(10, ChronoUnit.HOURS);
    private static final Instant STATUS_EFFECTIVE_DATE = Instant.now().minus(8, ChronoUnit.DAYS);
    private static final Instant MULTIPLIER_EFFECTIVE_DATE = Instant.now().minus(9, ChronoUnit.DAYS);

    private static final BigDecimal MULTIPLIER = new BigDecimal("123");

    private static final long ID = 123, OTHER_ID = 456;
    private static final String DEVICE_MRID = "DEVICE_MRID";
    private static final String DEVICE_TYPE = "DEVICE_TYPE";
    private static final String DEVICE_CONFIG_NAME = "DEVICE_CONFIG_NAME";
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String DEVICE_SERIAL_NUMBER = "DEVICE_SERIAL_NUMBER";
    private static final String BATCH = "BATCH";
    private static final String MANUFACTURER = "MANUFACTURER";
    private static final String MODEL_NUMBER = "MODEL_NUMBER";
    private static final String MODEL_VERSION = "MODEL_VERSION";
    private static final String OTHER_DEVICE_CONFIG_NAME = "OTHER_DEVICE_CONFIG_NAME";
    private static final String CONFIG_EVENT_REASON = EventReason.CHANGE_MULTIPLIER.getReason();
    private static final String CHANGE_STATUS_EVENT_REASON = EventReason.CHANGE_STATUS.getReason();
    private static final String UNKNOWN_EVENT_REASON = "UNKNOWN_EVENT_REASON";
    private static final String STATUS_VALUE = "STATUS_VALUE";
    private static final String PUBLIC_KEY_LABEL = "PUBLIC_KEY_LABEL";
    private static final String SYMMETRIC_KEY = "SYMMETRIC_KEY";
    private static final String SECURITY_ACCESSOR_NAME = "SECURITY_ACCESSOR_NAME";
    private static final String SECURITY_ACCESSOR_KEY = "SECURITY_ACCESSOR_KEY";
    private static final String CONNECTION_METHOD = "CONNECTION_METHOD";
    private static final String CONNECTION_ATTRIBUTE_NAME1 = "host";
    private static final String CONNECTION_ATTRIBUTE_NAME2 = "portNumber";
    private static final String CONNECTION_ATTRIBUTE_NAME3 = "connectionTimeout";

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    private BatchService batchService;

    @Mock
    private DeviceLifeCycleService deviceLifeCycleService;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private MeterConfigFaultMessageFactory faultMessageFactory;

    @Mock
    private DeviceType deviceType;

    @Mock
    private DeviceConfiguration deviceConfiguration, otherDeviceConfiguration;

    @Mock
    private ConnectionTask<?, ?> connectionTask;

    @Mock
    private ConnectionTypePluggableClass pluggableClass;

    @Mock
    private PropertySpec propertySpec;

    @Mock
    private Device device, otherDevice;

    @Mock
    private Finder<Device> deviceFinder;

    @Mock
    private com.energyict.mdc.device.data.DeviceBuilder deviceBuilder;

    @Mock
    private Supplier<FaultMessage> supplier;

    @Mock
    private FaultMessage faultMessage;

    @Mock
    private State status, newStatus;

    @Mock
    private Batch batch;

    @Mock
    private ExecutableAction executableAction;

    @Mock
    private AuthorizedTransitionAction authorizedTransitionAction;

    @Mock
    private MeteringTranslationService meteringTranslationService;

    @Mock
    private StateTransition stateTransition;

    @Mock
    private CIMLifecycleDates lifecycleDates;

    private MeterInfo meterInfo;

    private ConnectionAttributes attributes;

    private DeviceBuilder testable;

    @Before
    public void setUp() {
        testable = new DeviceBuilder(batchService, Clock.fixed(NOW, Clock.systemDefaultZone().getZone()),
                deviceLifeCycleService, deviceConfigurationService, deviceService, faultMessageFactory,
                meteringTranslationService);
        meterInfo = new MeterInfo();
        meterInfo.setDeviceType(DEVICE_TYPE);
        meterInfo.setDeviceConfigurationName(DEVICE_CONFIG_NAME);
        meterInfo.setDeviceName(DEVICE_NAME);
        meterInfo.setSerialNumber(DEVICE_SERIAL_NUMBER);
        meterInfo.setShipmentDate(SHIPMENT_DATE);
        meterInfo.setBatch(BATCH);
        meterInfo.setManufacturer(MANUFACTURER);
        meterInfo.setModelNumber(MODEL_NUMBER);
        meterInfo.setModelVersion(MODEL_VERSION);
        meterInfo.setMultiplier(MULTIPLIER);
        when(deviceConfigurationService.findDeviceTypeByName(DEVICE_TYPE)).thenReturn(Optional.of(deviceType));
        when(deviceConfiguration.getName()).thenReturn(DEVICE_CONFIG_NAME);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceService.findAllDevices(Mockito.notNull(Condition.class))).thenReturn(deviceFinder);
        when(deviceFinder.paged(0, 10)).thenReturn(deviceFinder);
        when(deviceFinder.find()).thenReturn(new ArrayList<>());
        when(deviceService.newDeviceBuilder(deviceConfiguration, DEVICE_NAME, SHIPMENT_DATE)).thenReturn(deviceBuilder);
        when(faultMessageFactory.meterConfigFaultMessageSupplier(Mockito.eq(DEVICE_NAME),
                Mockito.notNull(MessageSeeds.class), Mockito.anyVararg())).thenReturn(supplier);
        when(faultMessageFactory.meterConfigFaultMessageSupplier(Mockito.isNull(String.class),
                Mockito.notNull(MessageSeeds.class), Mockito.anyVararg())).thenReturn(supplier);
        when(supplier.get()).thenReturn(faultMessage);
        meterInfo.setmRID(DEVICE_MRID);
        meterInfo.setConfigurationEventReason(CONFIG_EVENT_REASON);
        meterInfo.setStatusValue(STATUS_VALUE);
        meterInfo.setStatusEffectiveDate(STATUS_EFFECTIVE_DATE);
        meterInfo.setMultiplierEffectiveDate(MULTIPLIER_EFFECTIVE_DATE);
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        SecurityInfo securityInfo = new SecurityInfo();
        securityInfo.setDeviceStatusesElementPresent(true);
        securityInfo.setDeviceStatuses(Arrays.asList(STATUS_VALUE, DefaultState.IN_STOCK.getKey()));
        SecurityKeyInfo securityKeyInfo = new SecurityKeyInfo();
        securityKeyInfo.setPublicKeyLabel(PUBLIC_KEY_LABEL);
        securityKeyInfo.setSymmetricKey(SYMMETRIC_KEY.getBytes());
        securityKeyInfo.setSecurityAccessorName(SECURITY_ACCESSOR_NAME);
        securityKeyInfo.setSecurityAccessorKey(SECURITY_ACCESSOR_KEY.getBytes());
        securityInfo.setSecurityKeys(Arrays.asList(securityKeyInfo));
        meterInfo.setSecurityInfo(securityInfo);
        attributes = new ConnectionAttributes();
        Attribute attribute1 = new Attribute();
        attribute1.setName(CONNECTION_ATTRIBUTE_NAME1);
        attribute1.setValue("hostName1");
        Attribute attribute2 = new Attribute();
        attribute2.setName(CONNECTION_ATTRIBUTE_NAME2);
        attribute2.setValue("4059");
        Attribute attribute3 = new Attribute();
        attribute3.setName(CONNECTION_ATTRIBUTE_NAME3);
        attribute3.setValue("1:12");
        attributes.getAttribute().addAll(Arrays.asList(attribute1, attribute2, attribute3));
        attributes.setConnectionMethod(CONNECTION_METHOD);
        meterInfo.setConnectionAttributes(Collections.singletonList(attributes));
        when(device.getId()).thenReturn(ID);
        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getState()).thenReturn(status);
        when(status.getName()).thenReturn(STATUS_VALUE);
        when(newStatus.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(batchService.findOrCreateBatch(BATCH)).thenReturn(batch);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getLifecycleDates()).thenReturn(lifecycleDates);
        when(connectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(propertySpec.getValueFactory()).thenReturn(mock(ValueFactory.class));
        when(propertySpec.getName()).thenReturn(CONNECTION_ATTRIBUTE_NAME1, CONNECTION_ATTRIBUTE_NAME2, CONNECTION_ATTRIBUTE_NAME3);
        when(pluggableClass.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec, propertySpec, propertySpec));
        when(connectionTask.getName()).thenReturn(CONNECTION_METHOD);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(deviceLifeCycleService.getExecutableActions(device)).thenReturn(Arrays.asList(executableAction));
        when(executableAction.getAction()).thenReturn(authorizedTransitionAction);
        when(authorizedTransitionAction.getStateTransition()).thenReturn(stateTransition);
        when(stateTransition.getTo()).thenReturn(newStatus);
    }

    @Test
    public void testPrepareCreateFrom_Success() throws FaultMessage {
        when(deviceBuilder.create()).thenReturn(device);
        Device result = testable.prepareCreateFrom(meterInfo).build();
        assertEquals(device, result);
        verify(deviceBuilder).withBatch(BATCH);
        verify(deviceBuilder).withSerialNumber(DEVICE_SERIAL_NUMBER);
        verify(deviceBuilder).withManufacturer(MANUFACTURER);
        verify(deviceBuilder).withModelNumber(MODEL_NUMBER);
        verify(deviceBuilder).withModelVersion(MODEL_VERSION);
        verify(deviceBuilder).withMultiplier(MULTIPLIER);
    }

    @Test
    public void testPrepareCreateFrom_NoSuchDeviceType() throws FaultMessage {
        when(deviceConfigurationService.findDeviceTypeByName(DEVICE_TYPE)).thenReturn(Optional.empty());
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_SUCH_DEVICE_TYPE,
                DEVICE_TYPE);
    }

    @Test
    public void testPrepareCreateFrom_DeviceConfigurationNameIsNull() throws FaultMessage {
        meterInfo.setDeviceConfigurationName(null);
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION);
    }

    @Test
    public void testPrepareCreateFrom_DeviceConfigurationNameDifferent() throws FaultMessage {
        meterInfo.setDeviceConfigurationName(OTHER_DEVICE_CONFIG_NAME);
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, OTHER_DEVICE_CONFIG_NAME);
    }

    @Test
    public void testPrepareCreateFrom_DeviceAlreadyExists() throws FaultMessage {
        when(deviceFinder.find()).thenReturn(Arrays.asList(device));
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NAME_AND_SERIAL_MUST_BE_UNIQUE);
    }

    @Test
    public void testPrepareChangeFrom_ChangeSecurityKeysAllowed_Success_NonDefaultState() throws FaultMessage {
        meterInfo.setShipmentDate(null);
        testable.prepareChangeFrom(meterInfo).build();
        verify(device).setName(DEVICE_NAME);
        verify(device).setSerialNumber(DEVICE_SERIAL_NUMBER);
        verify(device).setModelNumber(MODEL_NUMBER);
        verify(device).setModelVersion(MODEL_VERSION);
        verify(device).setManufacturer(MANUFACTURER);
        verify(device).save();
        verify(batch).addDevice(device);
    }

    @Test
    public void testPrepareChangeFrom_ChengeSecurityKeysAllowed_Success_DefaultState_InStock() throws FaultMessage {
        meterInfo.setStatusValue(DefaultState.IN_STOCK.getKey());
        meterInfo.getSecurityInfo().setDeviceStatuses(Arrays.asList(DefaultState.IN_STOCK.getDefaultFormat()));
        when(status.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(meteringTranslationService.getDisplayName(DefaultState.IN_STOCK))
                .thenReturn(DefaultState.IN_STOCK.getDefaultFormat());
        testable.prepareChangeFrom(meterInfo).build();
    }

    @Test
    public void testPrepareChangeFrom_ChengeSecurityKeysNotAllowed_Exception_NonDefaultState() throws FaultMessage {
        meterInfo.setStatusValue(STATUS_VALUE);
        meterInfo.getSecurityInfo().setDeviceStatuses(Arrays.asList(DefaultState.IN_STOCK.getDefaultFormat()));
        when(status.getName()).thenReturn(STATUS_VALUE);
        when(device.getName()).thenReturn(DEVICE_NAME);
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS, DEVICE_NAME, STATUS_VALUE);
    }

    @Test
    public void testPrepareChangeFrom_ChengeSecurityKeysNotAllowed_Exception_DefaultState_InStock()
            throws FaultMessage {
        meterInfo.setStatusValue(DefaultState.IN_STOCK.getKey());
        meterInfo.getSecurityInfo().setDeviceStatuses(Arrays.asList(STATUS_VALUE));
        when(meteringTranslationService.getDisplayName(DefaultState.IN_STOCK))
                .thenReturn(DefaultState.IN_STOCK.getDefaultFormat());
        when(status.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getName()).thenReturn(DEVICE_NAME);
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS, DEVICE_NAME,
                DefaultState.IN_STOCK.getDefaultFormat());
    }

    @Test
    public void testPrepareChangeFrom_ChengeSecurityKeysNotAllowed_Exception_StatusListEmpty() throws FaultMessage {
        meterInfo.setStatusValue(DefaultState.IN_STOCK.getKey());
        meterInfo.getSecurityInfo().setDeviceStatuses(new ArrayList<>());
        when(meteringTranslationService.getDisplayName(DefaultState.IN_STOCK))
                .thenReturn(DefaultState.IN_STOCK.getDefaultFormat());
        when(status.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getName()).thenReturn(DEVICE_NAME);
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS, DEVICE_NAME,
                DefaultState.IN_STOCK.getDefaultFormat());
    }

    @Test
    public void testPrepareChangeFrom_ChengeSecurityKeysAllowed_Success_DeviceStatusesElementNotPresent()
            throws FaultMessage {
        meterInfo.setStatusValue(DefaultState.IN_STOCK.getKey());
        meterInfo.getSecurityInfo().setDeviceStatusesElementPresent(false);
        meterInfo.getSecurityInfo().setDeviceStatuses(Arrays.asList(STATUS_VALUE));
        when(meteringTranslationService.getDisplayName(DefaultState.IN_STOCK))
                .thenReturn(DefaultState.IN_STOCK.getDefaultFormat());
        when(status.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getName()).thenReturn(DEVICE_NAME);
        testable.prepareChangeFrom(meterInfo).build();
    }

    @Test
    public void testPrepareChangeFrom_DeviceNotFoundByMridException() throws FaultMessage {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.empty());
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_DEVICE_WITH_MRID,
                DEVICE_MRID);
    }

    @Test
    public void testPrepareChangeFrom_ChangeSecurityKeysAllowed_Success_StatusChange_findDeviceByName()
            throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setShipmentDate(null);
        meterInfo.setDeviceConfigurationName(OTHER_DEVICE_CONFIG_NAME);
        meterInfo.setConfigurationEventReason(CHANGE_STATUS_EVENT_REASON);
        meterInfo.setStatusValue(DefaultState.IN_STOCK.getDefaultFormat());
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(otherDeviceConfiguration.getId()).thenReturn(763L);
        when(otherDeviceConfiguration.getName()).thenReturn(OTHER_DEVICE_CONFIG_NAME);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration, otherDeviceConfiguration));
        when(deviceService.changeDeviceConfigurationForSingleDevice(ID, 0, 763, 0)).thenReturn(device);
        testable.prepareChangeFrom(meterInfo).build();
        verify(executableAction).execute(STATUS_EFFECTIVE_DATE, Collections.emptyList());
    }

    @Test
    public void testPrepareChangeFrom_DeviceNotFoundByName() throws FaultMessage {
        meterInfo.setmRID(null);
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.empty());
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_DEVICE_WITH_NAME,
                DEVICE_NAME);
    }

    @Test
    public void testPrepareChangeFrom_ChangeSecurityKeysAllowed_DeviceConfigurationNotFound() throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setDeviceConfigurationName(OTHER_DEVICE_CONFIG_NAME);
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, OTHER_DEVICE_CONFIG_NAME);
    }

    @Test
    public void testPrepareChangeFrom_ChangeSecurityKeysAllowed_UnknownEventReason() throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setConfigurationEventReason(UNKNOWN_EVENT_REASON);
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration, otherDeviceConfiguration));
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME,
                MessageSeeds.NOT_VALID_CONFIGURATION_REASON, UNKNOWN_EVENT_REASON);
    }

    @Test
    public void testPrepareChangeFrom_NoSecurityKeysToUpdate_Success_StatusChange_findDeviceBySerialNumber()
            throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setDeviceName(null);
        meterInfo.setShipmentDate(null);
        meterInfo.getSecurityInfo().setSecurityKeys(new ArrayList<>());
        when(deviceService.findDevicesBySerialNumber(DEVICE_SERIAL_NUMBER)).thenReturn(Arrays.asList(device));
        testable.prepareChangeFrom(meterInfo).build();
        verify(device).setSerialNumber(DEVICE_SERIAL_NUMBER);
        verify(device).setModelNumber(MODEL_NUMBER);
        verify(device).setModelVersion(MODEL_VERSION);
        verify(device).setManufacturer(MANUFACTURER);
        verify(device).save();
        verify(batch).addDevice(device);
    }

    @Test
    public void testPrepareChangeFrom_ShipmentDate() throws FaultMessage {
        when(deviceService.findDevicesBySerialNumber(DEVICE_SERIAL_NUMBER)).thenReturn(Arrays.asList(device));
        when(status.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        testable.prepareChangeFrom(meterInfo).build();
        verify(device.getLifecycleDates()).setReceivedDate(SHIPMENT_DATE);
        verify(device).save();
        verify(batch).addDevice(device);
    }

    @Test
    @Ignore //remove test as now is possible to change shipment date via ws independent of the device state
    public void testPrepareChangeFrom_ShipmentDateNotInStock() {
        when(deviceService.findDevicesBySerialNumber(DEVICE_SERIAL_NUMBER)).thenReturn(Arrays.asList(device));
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.SHIPMENT_DATE_NOT_IN_STOCK);
    }

    @Test
    public void testPrepareChangeFrom_DeviceNotFouldBySerialNumber() throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setDeviceName(null);
        when(deviceService.findDevicesBySerialNumber(DEVICE_SERIAL_NUMBER)).thenReturn(new ArrayList<>());
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(null, MessageSeeds.NO_DEVICE_WITH_SERIAL_NUMBER,
                DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void testPrepareChangeFrom_NoSecurityKeysToUpdate_FoundBySerialNumber_DeviceNameNotUniqueException()
            throws FaultMessage {
        meterInfo.setmRID(null);
        meterInfo.setDeviceName(null);
        meterInfo.getSecurityInfo().setSecurityKeys(new ArrayList<>());
        meterInfo.setShipmentDate(null);
        when(deviceFinder.find()).thenReturn(Arrays.asList(otherDevice));
        when(otherDevice.getId()).thenReturn(OTHER_ID);
        when(deviceService.findDevicesBySerialNumber(DEVICE_SERIAL_NUMBER)).thenReturn(Arrays.asList(device));
        try {
            testable.prepareChangeFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(null, MessageSeeds.NAME_AND_SERIAL_MUST_BE_UNIQUE);
    }

    @Test
    public void testPrepareCreateFrom_NoConnectionMethodWithName() throws FaultMessage {
        when(connectionTask.getName()).thenReturn("Another name");
        when(deviceBuilder.create()).thenReturn(device);
        when(device.getName()).thenReturn(DEVICE_NAME);
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_CONNECTION_METHOD_WITH_NAME,
                CONNECTION_METHOD);
    }

    @Test
    public void testPrepareCreateFrom_NoConnectionMethods() throws FaultMessage {
        attributes.setConnectionMethod(null);
        meterInfo.setConnectionAttributes(Collections.singletonList(attributes));
        when(device.getConnectionTasks()).thenReturn(new ArrayList<>());
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(deviceBuilder.create()).thenReturn(device);
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_CONNECTION_METHODS);
    }

    @Test
    public void testPrepareCreateFrom_NoConnectionAttribute() throws FaultMessage {
        when(pluggableClass.getPropertySpecs()).thenReturn(new ArrayList<>());
        when(deviceBuilder.create()).thenReturn(device);
        when(connectionTask.getDevice()).thenReturn(device);
        when(device.getName()).thenReturn(DEVICE_NAME);
        try {
            testable.prepareCreateFrom(meterInfo).build();
            fail("Exception should be thrown");
        } catch (FaultMessage e) {
        }
        verify(faultMessageFactory).meterConfigFaultMessageSupplier(DEVICE_NAME, MessageSeeds.NO_CONNECTION_ATTRIBUTE,
                CONNECTION_ATTRIBUTE_NAME1, CONNECTION_METHOD);
    }
}
