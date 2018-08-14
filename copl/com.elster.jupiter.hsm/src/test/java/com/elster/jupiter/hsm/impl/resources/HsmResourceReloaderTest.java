package com.elster.jupiter.hsm.impl.resources;


import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HsmResourceReloaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    public HsmRefreshableResourceBuilder<HsmConfiguration> mockedLoader;

    private HsmResourceReloader<HsmConfiguration> reloader;

    @Test
    public void testNullLoader() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Could not instantiate resource re-loader based on null resource loader");
        reloader = new HsmResourceReloader<>(null);
    }

    @Test
    public void testNoReLoading() throws HsmBaseException {
        reloader = new HsmResourceReloader<>(mockedLoader);
        Mockito.when(mockedLoader.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.verify(mockedLoader, Mockito.times(1)).build();
    }

    @Test
    public void testReLoading() throws HsmBaseException {
        reloader = new HsmResourceReloader<>(mockedLoader);
        Mockito.when(mockedLoader.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader.timeStamp()).thenReturn(2L);
        reloader.load();
        Mockito.verify(mockedLoader, Mockito.times(2)).build();
    }

}
