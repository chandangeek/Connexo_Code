/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Provider;
import java.time.Clock;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AmrSystemImplTest extends EqualsContractTest {

    private static final int ID = 15;
    private static final String NAME = "name";
    public static final int INSTANCE_A_ID = 75;

    private AmrSystemImpl amrSystem, instanceA;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Provider<MeterActivationImpl> meterActivationFactory;
    @Mock
    private Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    @Mock
    private Provider<EndDeviceImpl> endDeviceFactory;
    @Mock
    private Clock clock;

    @Before
    public void setUp() {
        Provider<MeterImpl> meterFactory = () -> new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus, meterActivationFactory, metrologyConfigurationService);
        amrSystem = new AmrSystemImpl(dataModel, meteringService, meterFactory,endDeviceFactory).init(ID, NAME);
    }

    @Test
    public void testGetIdAfterCreation() {
        assertThat(amrSystem.getId()).isEqualTo(ID);
    }

    @Test
    public void testGetNameAfterCreation() {
        assertThat(amrSystem.getName()).isEqualTo(NAME);
    }

    @Test
    public void testNewMeter() {
        String amrId = "amrId";
        Meter meter = amrSystem.newMeter(amrId, "myName").create();

        assertThat(meter.getAmrId()).isEqualTo(amrId);
        assertThat(meter.getAmrSystem()).isEqualTo(amrSystem);
        assertThat(meter).isInstanceOf(MeterImpl.class);
    }

    @Test
    public void testPersist() {
        amrSystem.save();
        verify(dataModel).persist(amrSystem);
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            Provider<MeterImpl> meterFactory = mock(Provider.class);
            instanceA = new AmrSystemImpl(dataModel, meteringService, meterFactory, endDeviceFactory).init(ID, NAME);
            field("id").ofType(Integer.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Provider<MeterImpl> meterFactory = mock(Provider.class);
        AmrSystemImpl other = new AmrSystemImpl(dataModel, meteringService, meterFactory, endDeviceFactory).init(ID, NAME);
        field("id").ofType(Integer.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Provider<MeterImpl> meterFactory = mock(Provider.class);
        AmrSystemImpl other = new AmrSystemImpl(dataModel, meteringService, meterFactory, endDeviceFactory).init(ID, NAME);
        field("id").ofType(Integer.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return Collections.singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
