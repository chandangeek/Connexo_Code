package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
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

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private DataMapper<AmrSystem> factory;


    @Before
    public void setUp() {
        amrSystem = new AmrSystemImpl(ID, NAME);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
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
        when(serviceLocator.getOrmClient().getAmrSystemFactory()).thenReturn(factory);

        amrSystem.save();

        verify(factory).persist(amrSystem);
    }

}
