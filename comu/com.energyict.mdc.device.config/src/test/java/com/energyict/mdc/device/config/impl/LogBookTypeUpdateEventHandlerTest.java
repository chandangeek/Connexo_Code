/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.masterdata.LogBookType;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.config.impl.LogBookTypeUpdateEventHandler.OLD_OBIS_CODE_PROPERTY_NAME;
import static com.energyict.mdc.device.config.impl.LogBookTypeUpdateEventHandler.TOPIC;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LogBookTypeUpdateEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (11:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookTypeUpdateEventHandlerTest {

    private static final String OBIS_CODE = "0.0.99.98.0.255";
    private static final String OLD_OBIS_CODE = "1.0.99.97.0.255";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerDeviceConfigurationService deviceConfigurationService;

    private InMemoryBootstrapModule bootstrapModule;

    @Before
    public void setup () {
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new DataVaultModule(),
                new ThreadSecurityModule(this.principal),
                new OrmModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                //new IdsModule(),
                //new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule());
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(EventService.class);
            ctx.commit();
        }
    }

    @After
    public void cleanupDatabase () {
        this.bootstrapModule.deactivate();
    }

    @Before
    public void setupThesaurus () {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Test
    public void testUpdateObisCodeWhenNotInUse () {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getObisCode()).thenReturn(ObisCode.fromString(OBIS_CODE));
        when(this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType)).thenReturn(new ArrayList<>(0));
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(logBookType);
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(event.getType()).thenReturn(eventType);
        Event osgiEvent = new Event(TOPIC, this.noChangesEventProperties());
        when(event.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        this.newLogBookTypeUpdateEventHandler().handle(event);

        // Asserts
        verify(this.deviceConfigurationService).findDeviceConfigurationsUsingLogBookType(logBookType);
    }

    @Test(expected = CannotUpdateObisCodeWhenLogBookTypeIsInUseException.class)
    public void testUpdatedObisCodeWhenInUse () {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getObisCode()).thenReturn(ObisCode.fromString(OBIS_CODE));
        when(this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType)).thenReturn(Arrays.asList(deviceConfiguration));
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(logBookType);
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(event.getType()).thenReturn(eventType);
        Event osgiEvent = new Event(TOPIC, this.eventProperties(OLD_OBIS_CODE));
        when(event.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        this.newLogBookTypeUpdateEventHandler().handle(event);

        // Asserts
        verify(this.deviceConfigurationService).findDeviceConfigurationsUsingLogBookType(logBookType);
    }

    @Test
    public void testEventHandlerDelegatesToDeviceConfiguration () {
        ServerDeviceConfiguration deviceConfiguration = mock(ServerDeviceConfiguration.class);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getObisCode()).thenReturn(ObisCode.fromString(OBIS_CODE));
        when(this.deviceConfigurationService.findDeviceConfigurationsUsingLogBookType(logBookType)).thenReturn(deviceConfigurations);
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(logBookType);
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(event.getType()).thenReturn(eventType);
        Event osgiEvent = new Event(TOPIC, this.noChangesEventProperties());
        when(event.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        this.newLogBookTypeUpdateEventHandler().handle(event);

        // Asserts
        verify(this.deviceConfigurationService).findDeviceConfigurationsUsingLogBookType(logBookType);
    }

    private Map<String, ?> noChangesEventProperties() {
        return this.eventProperties(OBIS_CODE);
    }

    private Map<String, ?> eventProperties(String obisCode) {
        Map<String, String> properties = new HashMap<>();
        properties.put(OLD_OBIS_CODE_PROPERTY_NAME, obisCode);
        return properties;
    }

    private LogBookTypeUpdateEventHandler newLogBookTypeUpdateEventHandler() {
        LogBookTypeUpdateEventHandler eventHandler = new LogBookTypeUpdateEventHandler();
        eventHandler.setDeviceConfigurationService(this.deviceConfigurationService);
        eventHandler.setThesaurus(this.thesaurus);
        return eventHandler;
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }

    }

}