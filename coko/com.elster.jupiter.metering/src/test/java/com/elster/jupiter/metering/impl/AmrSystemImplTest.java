package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import java.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AmrSystemImplTest {

    private static final int ID = 15;
    private static final String NAME = "name";

    private AmrSystemImpl amrSystem;

    @Mock
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
    	Provider<MeterImpl> meterFactory = new Provider<MeterImpl>() {
			@Override
			public MeterImpl get() {
				return new MeterImpl(dataModel, eventService, deviceEventFactory, meteringService, thesaurus,meterActivationFactory);
			}
    	};
        amrSystem = new AmrSystemImpl(dataModel, meteringService, meterFactory,endDeviceFactory).init(ID, NAME);
    }

    @After
    public void tearDown() {
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
        Meter meter = amrSystem.newMeter(amrId);

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