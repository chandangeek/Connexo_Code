package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HsmReloadableConfigResourceTest {

    @Mock
    File f1;


    @Mock
    File f2;


    @Test(expected = HsmBaseException.class)
    public void testNullFile() throws HsmBaseException {
        HsmReloadableConfigResource.getInstance(null);
    }

    @Test(expected = HsmBaseException.class)
    public void testNotExstingFile() throws HsmBaseException {
        Mockito.when(f1.exists()).thenReturn(false);
        HsmReloadableConfigResource.getInstance(f1);
    }

    @Test
    public void testSingleton() throws HsmBaseException {
        Mockito.when(f1.exists()).thenReturn(true);
        Mockito.when(f2.exists()).thenReturn(true);
        HsmReloadableConfigResource instance = HsmReloadableConfigResource.getInstance(f1);
        Assert.assertEquals(false,instance.changed());
        Assert.assertEquals(instance, HsmReloadableConfigResource.getInstance(f1));
        Assert.assertEquals(false,instance.changed());
        Assert.assertEquals(instance, HsmReloadableConfigResource.getInstance(f2));
        Assert.assertEquals(true, instance.changed());
        Assert.assertEquals(instance, HsmReloadableConfigResource.getInstance(f2));
        Assert.assertEquals(false, instance.changed());
        Assert.assertEquals(instance, HsmReloadableConfigResource.getInstance(f1));
        Assert.assertEquals(true, instance.changed());
    }

}

