package com.elster.jupiter.hsm.impl.loader;


import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.resources.HsmReloadableResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HsmResourceLoaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    public HsmReloadableResource<HsmConfiguration> mockedLoader1;


    private HsmResourceLoader<HsmConfiguration> reloader;

    @Test
    public void testNullLoader() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Could not instantiate resource re-loader based on null resource loader");
        reloader = new HsmResourceLoader<>(null);
    }

    @Test
    public void testNoReLoading() throws HsmBaseException {
        reloader = new HsmResourceLoader<>(mockedLoader1);
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.verify(mockedLoader1, Mockito.times(1)).load();
        Mockito.verify(mockedLoader1, Mockito.times(0)).reload();
    }

    @Test
    public void testReLoading() throws HsmBaseException {
        reloader = new HsmResourceLoader<>(mockedLoader1);
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(2L);
        reloader.load();
        Mockito.verify(mockedLoader1, Mockito.times(1)).load();
        Mockito.verify(mockedLoader1, Mockito.times(1)).reload();
    }

}
