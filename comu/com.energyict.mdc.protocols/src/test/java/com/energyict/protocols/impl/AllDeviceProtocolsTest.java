/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocols.mdc.services.impl.InboundDeviceProtocolRule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;

import com.energyict.license.LicensedProtocolRule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the creation of all known device protocols contained in this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-03 (10:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class AllDeviceProtocolsTest {

    private static final Set<String> NO_LONGER_SUPPORTED = new HashSet<>();

    static {
        NO_LONGER_SUPPORTED.add("com.energyict.rtuprotocol.EIWeb");      //This old version of the EIWeb protocol is in mdwutil.jar, not available in Connexo
        NO_LONGER_SUPPORTED.add("com.energyict.rtuprotocol.Echelon");
        NO_LONGER_SUPPORTED.add("com.energyict.rtuprotocol.RtuServer");
    }

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
    private DataModel dataModel;
    @Mock
    private TransactionService transactionService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private SerialComponentService serialComponentService;
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
    private DeviceProtocolService deviceProtocolService;

    @Before
    public void initializeDatabase() throws SQLException {
        this.initializeMocks();
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector =
                Guice.createInjector(
                        new MockModule(),
                        this.bootstrapModule,
                        new ProtocolsModule());
        this.deviceProtocolService = injector.getInstance(DeviceProtocolService.class);
    }

    protected void initializeMocks() {
        when(principal.getName()).thenReturn("AllDeviceProtocolsTest");
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
    }

    @After
    public void cleanUpDatabase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    @Test
    public void testAllOutboundProtocols() {
        final Set<String> descriptions = new HashSet<>();

        Stream.of(LicensedProtocolRule.values())
                .filter(licensedProtocolRule -> (licensedProtocolRule.getCode() < 10000))
                .filter(licensedProtocolRule -> !NO_LONGER_SUPPORTED.contains(licensedProtocolRule.getClassName()))
                .forEach(rule -> this.testProtocolCreationAndUniqueDescription(rule, descriptions));
        System.out.println("Successfully tested the creation of " + LicensedProtocolRule.values().length + " outbound protocol(s)");
    }

    private void testProtocolCreationAndUniqueDescription(LicensedProtocolRule rule, Set<String> descriptions) {
        Object protocol = null;
        try {
            // Business method
            protocol = this.deviceProtocolService.createProtocol(rule.getClassName());
        } catch (Exception e) {
            fail("Failed to create protocol: " + rule.getClassName());
        }

        // Asserts
        assertThat(protocol).isNotNull();

        String description = null;
        if (protocol instanceof com.energyict.mdc.upl.MeterProtocol) {
            com.energyict.mdc.upl.MeterProtocol uplMeterProtocol = (com.energyict.mdc.upl.MeterProtocol) protocol;
            description = uplMeterProtocol.getProtocolDescription();
        } else if (protocol instanceof com.energyict.mdc.upl.SmartMeterProtocol) {
            com.energyict.mdc.upl.SmartMeterProtocol uplSmartMeterProtocol = (com.energyict.mdc.upl.SmartMeterProtocol) protocol;
            description = uplSmartMeterProtocol.getProtocolDescription();
        } else if (protocol instanceof com.energyict.mdc.upl.DeviceProtocol) {
            description = ((com.energyict.mdc.upl.DeviceProtocol) protocol).getProtocolDescription();
        }

        assertNotNull("Protocol " + rule.getClassName() + " has no description!", description);
        assertTrue("The description '" + description + "' of protocol '" + rule.getClassName() + "' was not unique!", descriptions.add(description));
    }

    @Test
    public void testAllInboundProtocols() {
        Stream.of(InboundDeviceProtocolRule.values()).forEach(this::testProtocolCreation);
        System.out.println("Successfully tested the creation of " + InboundDeviceProtocolRule.values().length + " inbound protocol(s)");
    }

    private void testProtocolCreation(InboundDeviceProtocolRule rule) {
        Object protocol = null;
        try {
            // Business method
            protocol = this.deviceProtocolService.createProtocol(rule.getProtocolTypeClass().getName());
        } catch (Exception e) {
            fail("Failed to create protocol: " + rule.getProtocolTypeClass().getName());
        }

        // Asserts
        assertThat(protocol).isNotNull();
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
            bind(IdentificationService.class).toInstance(identificationService);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(SerialComponentService.class).toInstance(serialComponentService);
            bind(DeviceMessageFileService.class).toInstance(deviceMessageFileService);
            bind(com.energyict.mdc.upl.nls.NlsService.class).toInstance(mock(com.energyict.mdc.upl.nls.NlsService.class));
            bind(com.energyict.mdc.upl.properties.Converter.class).toInstance(mock(com.energyict.mdc.upl.properties.Converter.class));
            bind(com.energyict.mdc.upl.properties.PropertySpecService.class).toInstance(mock(com.energyict.mdc.upl.properties.PropertySpecService.class));
            bind(DeviceMessageFileService.class).toInstance(deviceMessageFileService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(CertificateWrapperExtractor.class).toInstance(mock(CertificateWrapperExtractor.class));
        }
    }

}