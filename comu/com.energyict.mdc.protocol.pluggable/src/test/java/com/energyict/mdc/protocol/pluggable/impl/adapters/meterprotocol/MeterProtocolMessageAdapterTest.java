package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.pluggable.impl.DataModelInitializer;
import com.energyict.mdc.protocol.pluggable.impl.InMemoryPersistence;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SimpleLegacyMessageConverter;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link MeterProtocolMessageAdapter} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:05
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolMessageAdapterTest {

    private InMemoryPersistence inMemoryPersistence;
    private ProtocolPluggableServiceImpl protocolPluggableService;

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
        this.protocolPluggableService = this.inMemoryPersistence.getProtocolPluggableService();
        this.initializeMocks();
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    private void initializeMocks () {
        CollectedDataFactory collectedDataFactory = mock(CollectedDataFactory.class);
        when(collectedDataFactory.createEmptyCollectedMessageList()).thenReturn(mock(CollectedMessageList.class));
        ApplicationContext applicationContext = this.inMemoryPersistence.getApplicationContext();
        when(applicationContext.getModulesImplementing(CollectedDataFactory.class)).thenReturn(Arrays.asList(collectedDataFactory));

        DeviceProtocolMessageService deviceProtocolMessageService = this.inMemoryPersistence.getDeviceProtocolMessageService();
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(SimpleLegacyMessageConverter.class.getName())).
            thenReturn(new SimpleLegacyMessageConverter());
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
        new MeterProtocolMessageAdapter(simpleTestMeterProtocol, this.protocolPluggableService.getDataModel(), this.protocolPluggableService, this.inMemoryPersistence.getIssueService());

        // all is safe if no errors occur
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void testUnKnownMeterProtocol() {
        MeterProtocol meterProtocol = mock(MeterProtocol.class, withSettings().extraInterfaces(MessageProtocol.class));
        try {
            new MeterProtocolMessageAdapter(meterProtocol, this.protocolPluggableService.getDataModel(), this.protocolPluggableService, this.inMemoryPersistence.getIssueService());
        } catch (DeviceProtocolAdapterCodingExceptions e) {
            if (!e.getMessageId().equals("CSC-DEV-124")) {
                fail("Exception should have indicated that the given MeterProtocol is not known in the adapter mapping, but was " + e.getMessage());
            }
            throw e;
        }
        // should have gotten the exception
    }

    @Test
    public void testNotAMessageSupportClass() {
        MeterProtocol meterProtocol = new ThirdSimpleTestMeterProtocol();
        final MeterProtocolMessageAdapter protocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.protocolPluggableService.getDataModel(), this.protocolPluggableService, this.inMemoryPersistence.getIssueService());

        assertThat(protocolMessageAdapter.executePendingMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.updateSentMessages(Collections.<OfflineDeviceMessage>emptyList())).isInstanceOf(CollectedMessageList.class);
        assertThat(protocolMessageAdapter.format(null, null)).isEqualTo("");

        assertThat(protocolMessageAdapter.getSupportedMessages()).isEmpty();
    }

}