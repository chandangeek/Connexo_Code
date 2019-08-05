package com.elster.jupiter.hsm.impl.loader;


import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.resources.HsmRefreshableResourceBuilder;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.junit.Assert;
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
    public HsmRefreshableResourceBuilder<HsmConfiguration> mockedLoader1;

    @Mock
    public HsmRefreshableResourceBuilder<HsmConfiguration> mockedLoader2;

    private HsmResourceLoader<HsmConfiguration> reloader;

    @Test
    public void testNullLoader() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Could not instantiate resource re-loader based on null resource loader");
        reloader = HsmResourceLoader.getInstance(null);
    }

    @Test
    public void testNoReLoading() throws HsmBaseException {
        reloader = HsmResourceLoader.getInstance(mockedLoader1);
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.verify(mockedLoader1, Mockito.times(1)).build();
    }

    @Test
    public void testReLoading() throws HsmBaseException {
        reloader = HsmResourceLoader.getInstance(mockedLoader1);
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(1L);
        reloader.load();
        Mockito.when(mockedLoader1.timeStamp()).thenReturn(2L);
        reloader.load();
        Mockito.verify(mockedLoader1, Mockito.times(2)).build();
    }

    @Test
    public void testReLoadingInstance() throws HsmBaseException {
        Assert.assertEquals(HsmResourceLoader.getInstance(mockedLoader1), HsmResourceLoader.getInstance(mockedLoader1));
        Assert.assertNotEquals(HsmResourceLoader.getInstance(mockedLoader1), HsmResourceLoader.getInstance(mockedLoader2));
    }
}
