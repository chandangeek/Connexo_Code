package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Common methods for ComCommand tests
 *
 * @author gna
 * @since 31/05/12 - 16:28
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class CommonCommandImplTests extends AbstractComCommandExecuteTest {

    @Mock
    private EventPublisherImpl eventPublisher;

    @Before
    public void setupEventPublisher () {
        super.setupEventPublisher();
        EventPublisherImpl.setInstance(this.eventPublisher);
        when(this.eventPublisher.serviceProvider()).thenReturn(comServerEventServiceProviderAdapter());
    }

    @After
    public void resetEventPublisher () {
        super.resetEventPublisher();
        EventPublisherImpl.setInstance(null);
    }

    public static CommandRoot createCommandRoot() {
        return new CommandRootImpl(mock(OfflineDevice.class), AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
    }

    public static CommandRoot createCommandRoot(final OfflineDevice offlineDevice){
        return new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
    }

}