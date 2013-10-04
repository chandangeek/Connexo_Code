package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesDataStorer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplTest {

    private ReadingStorerImpl readingStorer;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private TimeSeriesDataStorer storer;

    @Before
    public void setUp() {
        when(serviceLocator.getIdsService().createStorer(true)).thenReturn(storer);

        readingStorer = new ReadingStorerImpl(true);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAdd() {

    }

}
