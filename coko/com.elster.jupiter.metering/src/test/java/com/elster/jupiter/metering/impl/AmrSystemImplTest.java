package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmrSystemImplTest {

    private static final int ID = 15;
    private static final String NAME = "name";

    private AmrSystemImpl amrSystem;

    @Mock
    private DataMapper<AmrSystem> factory;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(AmrSystemImpl.class)).thenReturn(new AmrSystemImpl(dataModel, meteringService));
        when(dataModel.getInstance(MeterImpl.class)).thenReturn(new MeterImpl(dataModel, eventService, meteringService, thesaurus));

        amrSystem = AmrSystemImpl.from(dataModel, ID, NAME);
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
        when(dataModel.mapper(AmrSystem.class)).thenReturn(factory);

        amrSystem.save();

        verify(factory).persist(amrSystem);
    }

}
