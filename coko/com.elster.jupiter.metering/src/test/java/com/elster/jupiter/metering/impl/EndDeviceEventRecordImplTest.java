/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceEventTypeCodeBuilder;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.LocalEventImpl;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PublisherImpl;

import com.google.common.collect.ImmutableList;
import org.osgi.service.event.Event;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceEventRecordImplTest extends EqualsContractTest {

    private static final long END_DEVICE_ID = 185L;
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    private EndDeviceEventRecordImpl instanceA;

    @Mock
    private EndDevice endDevice, endDevice2;
    @Mock
    private EndDeviceEventType endDeviceEventType, endDeviceEventType2;
    @Mock
    private DataModel dataModel;
    @Mock
    private Subscriber subscriber;

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Test
    @Transactional
    public void testPersist() throws SQLException {
        when(subscriber.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
        ((PublisherImpl) inMemoryBootstrapModule.getPublisher()).addHandler(subscriber);
        ServerMeteringService meteringService = getMeteringService();
        DataModel dataModel = meteringService.getDataModel();
        Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.BATTERY)
                .subDomain(EndDeviceSubDomain.CHARGE)
                .eventOrAction(EndDeviceEventOrAction.DECREASED)
                .toCode();
        EndDeviceEventTypeImpl eventType = meteringService.createEndDeviceEventType(code);

        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        EndDevice endDevice = amrSystem.createEndDevice("amrID", "DeviceName");
        EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date).create();

        assertThat(dataModel.mapper(EndDeviceEventRecord.class).getOptional(endDevice.getId(), eventType.getMRID(), date).get()).isEqualTo(endDeviceEventRecord);
        ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
        verify(subscriber, times(2)).handle(localEventCapture.capture());

        LocalEvent localEvent = localEventCapture.getAllValues().get(1);
        Assertions.assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.END_DEVICE_EVENT_CREATED.topic());
        Event event = localEvent.toOsgiEvent();
        Assertions.assertThat(event.containsProperty("endDeviceId")).isTrue();
        Assertions.assertThat(event.containsProperty("endDeviceEventType")).isTrue();
        Assertions.assertThat(event.containsProperty("eventTimestamp")).isTrue();
    }

    @Test
    @Transactional
    public void testPersistWithProperties() throws SQLException {
        ServerMeteringService meteringService = getMeteringService();
        DataModel dataModel = meteringService.getDataModel();
        Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.BATTERY)
                .subDomain(EndDeviceSubDomain.CHARGE)
                .eventOrAction(EndDeviceEventOrAction.DECREASED)
                .toCode();
        EndDeviceEventTypeImpl eventType = meteringService.createEndDeviceEventType(code);

        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        EndDevice endDevice = amrSystem.createEndDevice("amrID", "DeviceName");
        EndDeviceEventRecord endDeviceEventRecord = endDevice.addEventRecord(eventType, date)
                .addProperty("A", "C")
                .addProperty("D", "C")
                .create();

        Optional<EndDeviceEventRecord> found = dataModel.mapper(EndDeviceEventRecord.class).getOptional(endDevice.getId(), eventType.getMRID(), date);
        assertThat(found.get()).isEqualTo(endDeviceEventRecord);
        assertThat(found.get().getProperties()).contains(entry("A", "C"), entry("D", "C"));
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        DataModel dataModel = mock(DataModel.class);
        if (instanceA == null) {
            when(endDevice.getId()).thenReturn(END_DEVICE_ID);
            when(endDevice2.getId()).thenReturn(END_DEVICE_ID + 1);
            when(endDeviceEventType.getMRID()).thenReturn("A");
            when(endDeviceEventType2.getMRID()).thenReturn("B");
            instanceA = new EndDeviceEventRecordImpl(dataModel, null).init(endDevice, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant());
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        DataModel dataModel = mock(DataModel.class);
        when(dataModel.getInstance(EndDeviceEventRecordImpl.class)).thenReturn(new EndDeviceEventRecordImpl(dataModel, null));
        return new EndDeviceEventRecordImpl(dataModel, null).init(endDevice, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant());
    }

    EndDeviceEventRecordImpl createEndDeviceEvent() {
        return new EndDeviceEventRecordImpl(dataModel, null);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(
                createEndDeviceEvent().init(endDevice2, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant()),
                createEndDeviceEvent().init(endDevice, endDeviceEventType2, ZonedDateTime.of(2013, 12, 17, 14, 41, 0, 0, ZoneId.systemDefault()).toInstant()),
                createEndDeviceEvent().init(endDevice, endDeviceEventType, ZonedDateTime.of(2013, 12, 17, 14, 42, 0, 0, ZoneId.systemDefault()).toInstant())
        );
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
