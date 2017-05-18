/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.channels.EmptyConnectionType;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.channels.inbound.EIWebPlusConnectionType;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioCaseModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.channels.serial.rf.WavenisSerialConnectionType;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocols.mdc.services.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the creation of all known connection types contained in this bundle.
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class AllConnectionTypesTest {

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Principal principal;
    @Mock
    private Clock clock;
    @Mock
    private LicenseService licenseService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private OrmService ormService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private IssueService issueService;
    @Mock
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private CalendarService calendarService;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private DeviceMessageFileService deviceMessageFileService;

    private InMemoryBootstrapModule bootstrapModule;
    private ConnectionTypeService connectionTypeService;

    @Before
    public void initializeDatabase() throws SQLException {
        this.initializeMocks();
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector =
                Guice.createInjector(
                        new MockModule(),
                        this.bootstrapModule,
                        new ProtocolsModule());
        this.connectionTypeService = this.getConnectionTypeService(injector);
    }

    private ConnectionTypeService getConnectionTypeService(Injector injector) {
        return ConnectionTypeServiceImpl.withAllSerialComponentServices();
    }

    protected void initializeMocks() {
        when(principal.getName()).thenReturn("AllConnectionTypesTest");
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @After
    public void cleanUpDatabase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    @Test
    public void createSioPaknetModemConnectionType() {
        this.testCreateInstance(SioPaknetModemConnectionType.class);
    }

    @Test
    public void createSioPEMPModemConnectionType() {
        this.testCreateInstance(SioPEMPModemConnectionType.class);
    }

    @Test
    public void createSioCaseModemConnectionType() {
        this.testCreateInstance(SioCaseModemConnectionType.class);
    }

    @Test
    public void createSioOpticalConnectionType() {
        this.testCreateInstance(SioOpticalConnectionType.class);
    }

    @Test
    public void createSioAtModemConnectionType() {
        this.testCreateInstance(SioAtModemConnectionType.class);
    }

    @Test
    public void createSioSerialConnectionType() {
        this.testCreateInstance(SioSerialConnectionType.class);
    }

    @Test
    public void createRxTxOpticalConnectionType() {
        this.testCreateInstance(RxTxOpticalConnectionType.class);
    }

    @Test
    public void createRxTxAtModemConnectionType() {
        this.testCreateInstance(RxTxAtModemConnectionType.class);
    }

    @Test
    public void createRxTxSerialConnectionType() {
        this.testCreateInstance(RxTxSerialConnectionType.class);
    }

    @Test
    public void createEmptyConnectionType() {
        this.testCreateInstance(EmptyConnectionType.class);
    }

    @Test
    public void createOutboundTcpIpConnectionType() {
        this.testCreateInstance(OutboundTcpIpConnectionType.class);
    }

    @Test
    public void createTcpIpPostDialConnectionType() {
        this.testCreateInstance(TcpIpPostDialConnectionType.class);
    }

    @Test
    public void createOutboundUdpConnectionType() {
        this.testCreateInstance(OutboundUdpConnectionType.class);
    }

    @Test
    public void createWavenisGatewayConnectionType() {
        this.testCreateInstance(WavenisGatewayConnectionType.class);
    }

    @Test
    public void createTLSConnectionType() {
        this.testCreateInstance(TLSConnectionType.class);
    }

    @Test
    public void createCTRInboundDialHomeIdConnectionType() {
        this.testCreateInstance(CTRInboundDialHomeIdConnectionType.class);
    }

    @Test
    public void createWavenisSerialConnectionType() {
        this.testCreateInstance(WavenisSerialConnectionType.class);
    }

    @Test
    public void createInboundProximusSmsConnectionType() {
        this.testCreateInstance(InboundProximusSmsConnectionType.class);
    }

    @Test
    public void createOutboundProximusSmsConnectionType() {
        this.testCreateInstance(OutboundProximusSmsConnectionType.class);
    }

    @Test
    public void createInboundIpConnectionType() {
        this.testCreateInstance(InboundIpConnectionType.class);
    }

    @Test
    public void createEIWebPlusConnectionType() {
        this.testCreateInstance(EIWebPlusConnectionType.class);
    }

    @Test
    public void createEIWebConnectionType() {
        this.testCreateInstance(EIWebConnectionType.class);
    }

    private <T extends com.energyict.mdc.upl.io.ConnectionType> void testCreateInstance(Class<T> connectionTypeClass) {
        // Business method
        ConnectionType connectionType = this.connectionTypeService.createConnectionType(connectionTypeClass.getName());

        // Asserts
        assertThat(connectionType).isNotNull();
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(Clock.class).toInstance(clock);
            bind(LicenseService.class).toInstance(licenseService);
            bind(MeteringService.class).toInstance(meteringService);
            bind(NlsService.class).toInstance(nlsService);
            bind(MessageInterpolator.class).toInstance(thesaurus);
            bind(OrmService.class).toInstance(ormService);
            bind(TransactionService.class).toInstance(transactionService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(IssueService.class).toInstance(issueService);
            bind(MdcReadingTypeUtilService.class).toInstance(mdcReadingTypeUtilService);
            bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
            bind(CalendarService.class).toInstance(calendarService);
            bind(CollectedDataFactory.class).toInstance(collectedDataFactory);
            bind(com.energyict.mdc.upl.nls.NlsService.class).toInstance(mock(com.energyict.mdc.upl.nls.NlsService.class));
            bind(com.energyict.mdc.upl.properties.Converter.class).toInstance(mock(com.energyict.mdc.upl.properties.Converter.class));
            bind(com.energyict.mdc.upl.properties.PropertySpecService.class).toInstance(mock(com.energyict.mdc.upl.properties.PropertySpecService.class));
            bind(DeviceMessageFileService.class).toInstance(deviceMessageFileService);
            bind(IdentificationService.class).toInstance(identificationService);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DeviceMessageFileService.class).toInstance(deviceMessageFileService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }
}