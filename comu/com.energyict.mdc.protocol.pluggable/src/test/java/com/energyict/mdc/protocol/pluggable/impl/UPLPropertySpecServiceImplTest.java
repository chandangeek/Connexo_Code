package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoTranslationKeyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLPropertySpecServiceImpl;
import com.energyict.mdc.upl.nls.NlsMessageFormat;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UPLPropertySpecServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (14:30)
 */
@RunWith(MockitoJUnitRunner.class)
public class UPLPropertySpecServiceImplTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private Thesaurus uplThesaurus;
    @Mock
    private com.elster.jupiter.nls.Thesaurus thesaurus;
    @Mock
    private NlsService nlsService;

    private UPLPropertySpecServiceImpl propertySpecService;

    @BeforeClass
    public static void staticSetUp() {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new ThreadSecurityModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new TimeModule(),
                new PubSubModule(),
                new UtilModule(),
                new BasicPropertiesModule(),
                new OrmModule(),
                new NlsModule(),
                new DataVaultModule(),
                new InMemoryMessagingModule(),
                new TransactionModule(false));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(OrmService.class); // fake call to make sure component is initialized
            injector.getInstance(PropertySpecService.class); // fake call to make sure component is initialized
            injector.getInstance(DataVaultService.class); // fake call to make sure component is initialized
            ctx.commit();
        }
    }

    @AfterClass
    public static void staticTearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void initializeThesaurus() {
        Stream.of(TranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(TranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        when(this.uplThesaurus.getFormat(translationKey)).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(ConnexoTranslationKeyAdapter.class)))
            .thenAnswer(invocation -> {
                com.elster.jupiter.nls.NlsMessageFormat messageFormat1 = mock(com.elster.jupiter.nls.NlsMessageFormat.class);
                ConnexoTranslationKeyAdapter adapter = (ConnexoTranslationKeyAdapter) invocation.getArguments()[0];
                if (adapter != null) {
                    TranslationKey uplTranslationKey = adapter.getActual();
                    when(messageFormat1.format(anyVararg())).thenReturn(uplTranslationKey.getDefaultFormat());
                }
                return messageFormat1;
            });
    }

    @Before
    public void initializePropertySpecService() throws Exception {
        when(this.nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
        DataVaultService dataVaultService = injector.getInstance(DataVaultService.class);
        this.propertySpecService =
                new UPLPropertySpecServiceImpl(
                        new PropertySpecServiceImpl(
                                injector.getInstance(PropertySpecService.class),
                                dataVaultService,
                                injector.getInstance(OrmService.class)),
                        this.nlsService);
    }

    @Test
    public void testStringSpecWithHardCodedValues() {
        String expectedName = "name";
        String expectedDisplayName = "displayName";
        String expectedDescription = "description";

        // Business method
        PropertySpec propertySpec =
                this.propertySpecService
                    .stringSpec()
                        .named(expectedName, expectedDisplayName)
                        .describedAs(expectedDescription)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(expectedName);
        assertThat(propertySpec.getDisplayName()).isEqualTo(expectedDisplayName);
        assertThat(propertySpec.getDescription()).isEqualTo(expectedDescription);
        assertThat(propertySpec.isRequired()).isFalse();
        assertThat(propertySpec.getDefaultValue()).isEmpty();
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    @Test
    public void testStringSpecWithTranslationKeys() {
        // Business method
        PropertySpec propertySpec =
                this.propertySpecService
                    .stringSpec()
                    .named(TranslationKeys.NAME)
                    .describedAs(TranslationKeys.DESCRIPTION)
                    .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(TranslationKeys.NAME.getKey());
        assertThat(propertySpec.getDisplayName()).isEqualTo(TranslationKeys.NAME.getDefaultFormat());
        assertThat(propertySpec.getDescription()).isEqualTo(TranslationKeys.DESCRIPTION.getDefaultFormat());
        assertThat(propertySpec.isRequired()).isFalse();
        assertThat(propertySpec.getDefaultValue()).isEmpty();
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    @Test
    public void testStringSpecWithHardCodeNameAndTranslationKeys() {
        String expectedName = "name";

        // Business method
        PropertySpec propertySpec =
                this.propertySpecService
                    .stringSpec()
                    .named(expectedName, TranslationKeys.DISPLAYNAME)
                    .describedAs(TranslationKeys.DESCRIPTION)
                    .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(expectedName);
        assertThat(propertySpec.getDisplayName()).isEqualTo(TranslationKeys.DISPLAYNAME.getDefaultFormat());
        assertThat(propertySpec.getDescription()).isEqualTo(TranslationKeys.DESCRIPTION.getDefaultFormat());
        assertThat(propertySpec.isRequired()).isFalse();
        assertThat(propertySpec.getDefaultValue()).isEmpty();
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    private static class MockModule extends AbstractModule {
        private BundleContext bundleContext;
        private EventAdmin eventAdmin;

        private MockModule(BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = mock(EventAdmin.class);
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(UserService.class).toInstance(mock(UserService.class));
        }
    }

    private enum TranslationKeys implements TranslationKey {
        NAME, DISPLAYNAME, DESCRIPTION;

        @Override
        public String getKey() {
            return "com.energyict.mdc.pluggable.impl.TranslationKeys." + this.name().toLowerCase();
        }

        @Override
        public String getDefaultFormat() {
            return this.name();
        }
    }

}