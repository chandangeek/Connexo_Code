package com.energyict.protocols.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.protocols.mdc.InboundDeviceProtocolRule;
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
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private SerialComponentService serialComponentService;
    @Mock
    private IssueService issueService;
    @Mock
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    @Mock
    private UserFileFactory userFileFactory;
    @Mock
    private CodeFactory codeFactory;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;

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
                        new MdcIOModule(),
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
    public void cleanUpDatabase() throws BusinessException, SQLException {
        this.bootstrapModule.deactivate();
    }

    @Test
    public void testAllOutboundProtocols() {
        Stream.of(LicensedProtocolRule.values()).forEach(this::testProtocolCreation);
        System.out.println("Successfully tested the creation of " + LicensedProtocolRule.values().length + " outbound protocol(s)");
    }

    private void testProtocolCreation(LicensedProtocolRule rule) {
        Object protocol = null;
        try {
            // Business method
            protocol = this.deviceProtocolService.createProtocol(rule.getClassName());
        }
        catch (Exception e) {
            fail("Failed to create protocol: " + rule.getClassName());
        }

        // Asserts
        assertThat(protocol).isNotNull();
    }

    @Test
    public void testAllInbboundProtocols() {
        Stream.of(InboundDeviceProtocolRule.values()).forEach(this::testProtocolCreation);
        System.out.println("Successfully tested the creation of " + InboundDeviceProtocolRule.values().length + " inbound protocol(s)");
    }

    private void testProtocolCreation(InboundDeviceProtocolRule rule) {
        Object protocol = null;
        try {
            // Business method
            protocol = this.deviceProtocolService.createProtocol(rule.getProtocolTypeClass().getName());
        }
        catch (Exception e) {
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
            bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(TopologyService.class).toInstance(topologyService);
            bind(IssueService.class).toInstance(issueService);
            bind(MdcReadingTypeUtilService.class).toInstance(mdcReadingTypeUtilService);
            bind(UserFileFactory.class).toInstance(userFileFactory);
            bind(CodeFactory.class).toInstance(codeFactory);
            bind(CollectedDataFactory.class).toInstance(collectedDataFactory);
            bind(IdentificationService.class).toInstance(identificationService);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(SerialComponentService.class).toInstance(serialComponentService);
        }
    }

}