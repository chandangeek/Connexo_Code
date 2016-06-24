package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.firmware.FirmwareVersion;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 27/05/2016 - 15:04
 */
public class FirmwareCacheTest {

    private static final byte[] CONTENT = new byte[20];

    @Test
    public void testTempFileCreation() throws IOException {

        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getId()).thenReturn(1L);
        when(firmwareVersion.getModTime()).thenReturn(Instant.ofEpochMilli(10000000L));
        when(firmwareVersion.getFirmwareFileAsStream()).thenReturn(new ByteArrayInputStream(CONTENT));

        File tempFile = FirmwareCache.findOrCreateTempFile(firmwareVersion);
        String path = tempFile.getAbsolutePath();

        assertTrue(new File(path).exists());

        byte[] bytes = Files.readAllBytes(Paths.get(path));

        Assert.assertArrayEquals(bytes, CONTENT);
    }
}