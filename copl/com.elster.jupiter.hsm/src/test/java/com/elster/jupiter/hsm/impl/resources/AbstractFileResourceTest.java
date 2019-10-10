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
public class AbstractFileResourceTest {

    @Mock
    public File file1;

    @Mock
    public File file2;

    private AbstractFileResource fileResource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test(expected = HsmBaseException.class)
    public void testNullFileConstructor() throws HsmBaseException {
        new AbstractFileReourceTestImpl(null);
    }

    @Test(expected = HsmBaseException.class)
    public void testNonExistingFileConstructor() throws HsmBaseException {
        Mockito.when(file1.exists()).thenReturn(false);
        new AbstractFileReourceTestImpl(file1);
    }

    @Test(expected = HsmBaseException.class)
    public void testSetNullFile() throws HsmBaseException {
        Mockito.when(file1.exists()).thenReturn(true);
        long timeStamp = 1L;
        Mockito.when(file1.lastModified()).thenReturn(timeStamp);
        fileResource = new AbstractFileReourceTestImpl(file1);
        fileResource.setFile(null);
    }

    @Test(expected = HsmBaseException.class)
    public void testSetNotExistingFile() throws HsmBaseException {
        Mockito.when(file1.exists()).thenReturn(true);
        long timeStamp = 1L;
        Mockito.when(file1.lastModified()).thenReturn(timeStamp);
        fileResource = new AbstractFileReourceTestImpl(file1);

        Mockito.when(file2.exists()).thenReturn(false);
        fileResource.setFile(file2);
    }

    @Test
    public void testInitResource() throws HsmBaseException {
        Mockito.when(file1.exists()).thenReturn(true);
        long timeStamp = 1L;
        Mockito.when(file1.lastModified()).thenReturn(timeStamp);
        fileResource = new AbstractFileReourceTestImpl(file1);
        Assert.assertEquals(file1, fileResource.getFile());
        Assert.assertEquals(Long.valueOf(timeStamp), fileResource.timeStamp());
        Assert.assertFalse(fileResource.changed());
    }

    @Test
    public void testChangetResource() throws HsmBaseException {
        long timeStamp1 = 1L;
        Mockito.when(file1.exists()).thenReturn(true);
        Mockito.when(file1.lastModified()).thenReturn(timeStamp1);
        fileResource = new AbstractFileReourceTestImpl(file1);
        Assert.assertEquals(file1, fileResource.getFile());
        Assert.assertEquals(Long.valueOf(timeStamp1), fileResource.timeStamp());
        Assert.assertFalse(fileResource.changed());
        // change file
        long timeStamp2 = 2L;
        Mockito.when(file2.exists()).thenReturn(true);
        Mockito.when(file2.lastModified()).thenReturn(timeStamp2);
        fileResource.setFile(file2);
        Assert.assertEquals(file2, fileResource.getFile());
        Assert.assertEquals(Long.valueOf(timeStamp2), fileResource.timeStamp());
        Assert.assertTrue(fileResource.changed());
        // after previous call we reset change flag, this is desired behavior
        Assert.assertFalse(fileResource.changed());
    }


    public static class AbstractFileReourceTestImpl extends AbstractFileResource<Object> {
        public AbstractFileReourceTestImpl(File f) throws HsmBaseException {
            super(f);
        }

        // this method shall not be tested
        @Override
        public Object load() throws HsmBaseException {
            return null;
        }

        // this method shall not be tested
        @Override
        public Object reload() throws HsmBaseException {
            return null;
        }
    }
}
