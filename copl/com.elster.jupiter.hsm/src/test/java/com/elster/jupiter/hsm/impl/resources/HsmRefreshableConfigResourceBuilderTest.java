package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;


import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HsmRefreshableConfigResourceBuilderTest {

    @Mock
    public File file;

    private HsmRefreshableConfigResourceBuilder hsmConfigLoader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNullResource() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        hsmConfigLoader = new HsmRefreshableConfigResourceBuilder(null);
    }

    @Test
    public void testNonExistingFile() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        hsmConfigLoader = new HsmRefreshableConfigResourceBuilder(new File("nonExistingFile"));
    }

    @Test
    public void testTimeStamp() throws HsmBaseException {
        Mockito.when(file.exists()).thenReturn(true);
        long timeStamp = 1L;
        Mockito.when(file.lastModified()).thenReturn(timeStamp);
        hsmConfigLoader = new HsmRefreshableConfigResourceBuilder(file);
        Assert.assertTrue(timeStamp == hsmConfigLoader.timeStamp());
    }

}
