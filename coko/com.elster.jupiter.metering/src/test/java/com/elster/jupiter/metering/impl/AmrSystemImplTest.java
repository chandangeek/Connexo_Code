package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AmrSystemImplTest {

    private static final int ID = 15;
    private static final String NAME = "name";

    private AmrSystemImpl amrSystem;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
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
    	Provider<MeterImpl> meterFactory = () -> new MeterImpl(clock, dataModel, eventService, deviceEventFactory, meteringService, thesaurus,meterActivationFactory);
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
        Meter meter = amrSystem.newMeter(amrId).create();

        assertThat(meter.getAmrId()).isEqualTo(amrId);
        assertThat(meter.getAmrSystem()).isEqualTo(amrSystem);
        assertThat(meter).isInstanceOf(MeterImpl.class);
    }

    @Test
    public void testPersist() {
        amrSystem.save();
        verify(dataModel).persist(amrSystem);
    }

}