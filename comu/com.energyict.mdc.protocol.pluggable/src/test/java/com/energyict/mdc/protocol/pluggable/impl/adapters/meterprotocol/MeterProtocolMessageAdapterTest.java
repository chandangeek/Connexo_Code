package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.impl.DataModelInitializer;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;

import com.elster.jupiter.orm.DataModel;

import java.sql.SQLException;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link MeterProtocolMessageAdapter} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:05
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolMessageAdapterTest {

    @Mock
    private CollectedDataFactory collectedDataFactory;

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeDatabaseAndMocks () {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase(
                "MeterProtocolMessageAdapterTest.mdc.protocol.pluggable",
                new DataModelInitializer() {
                    @Override
                    public void initializeDataModel(DataModel dataModel) {
                        initializeMessageAdapterMappingFactory(dataModel);
                    }
                });
        this.propertySpecService = this.inMemoryPersistence.getPropertySpecService();
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.initializeMocks();
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    private void initializeMocks () {
        DeviceProtocolMessageService deviceProtocolMessageService = this.inMemoryPersistence.getDeviceProtocolMessageService();
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(SimpleLegacyMessageConverter.class.getName())).
            thenReturn(new SimpleLegacyMessageConverter(propertySpecService));
        doThrow(DeviceProtocolAdapterCodingExceptions.class).
            when(deviceProtocolMessageService).createDeviceProtocolMessagesFor("com.energyict.comserver.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
    }

    private void initializeMessageAdapterMappingFactory(DataModel dataModel) {
        this.initializeMessageAdapterMappingFactory(dataModel, SimpleTestMeterProtocol.class, SimpleLegacyMessageConverter.class.getName());
        this.initializeMessageAdapterMappingFactory(dataModel, SecondSimpleTestMeterProtocol.class, "com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.Certainly1NotKnown2ToThisClass3PathLegacyConverter");
        this.initializeMessageAdapterMappingFactory(dataModel, ThirdSimpleTestMeterProtocol.class, ThirdSimpleTestMeterProtocol.class.getName());
    }

    private void initializeMessageAdapterMappingFactory(DataModel dataModel, Class meterProtocolTestClass, String legacyMessageConverterClassName) {
        MessageAdapterMappingImpl messageAdapterMapping =
                new MessageAdapterMappingImpl(meterProtocolTestClass.getName(), legacyMessageConverterClassName);
        dataModel.persist(messageAdapterMapping);
    }

    @Test
    public void testKnownMeterProtocol() {
        SimpleTestMeterProtocol simpleTestMeterProtocol = new SimpleTestMeterProtocol();
        DataModel dataModel = this.protocolPluggableService.getDataModel();
        new MeterProtocolMessageAdapter(
                simpleTestMeterProtocol,
                dataModel,
                new MessageAdapterMappingFactoryImpl(dataModel),
                this.protocolPluggableService,
                this.inMemoryPersistence.getIssueService(),
                this.collectedDataFactory);

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new MeterProtocolMessageAdapter(meterProtocol, this.protocolPluggableService.getDataModel(), mock(MessageAdapterMappingFactory.class), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory);
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageSeed().equals(MessageSeeds.NON_EXISTING_MAP_ELEMENT)) {
                fail("Exception should have indicated that the given MeterProtocol is not known in the adapter mapping, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotAMessageSupportClass() {
        MeterProtocol meterProtocol = new ThirdSimpleTestMeterProtocol();
        MeterProtocolMessageAdapter protocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.protocolPluggableService.getDataModel(), mock(MessageAdapterMappingFactory.class), this.protocolPluggableService, this.inMemoryPersistence.getIssueService(), this.collectedDataFactory);
        when(this.collectedDataFactory.createEmptyCollectedMessageList()).thenReturn(mock(CollectedMessageList.class));

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isNotNull();
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isNotNull();
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

}