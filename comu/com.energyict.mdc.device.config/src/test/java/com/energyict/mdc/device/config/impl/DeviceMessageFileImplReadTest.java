/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceMessageFileImpl#readWith(Consumer)} method.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileImplReadTest {

    @Mock
    private FileSystem fileSystem;
    @Mock
    private FileSystemProvider fileSystemProvider;
    @Mock
    private Path path;
    @Mock
    private Path filePart;
    @Mock
    private InputStream pathIS;
    @Mock
    private DeviceType deviceType;

    private boolean readCalled = false;

    @Before
    public void initializeMocks() throws IOException {
        when(this.fileSystem.provider()).thenReturn(this.fileSystemProvider);
        when(this.filePart.toString()).thenReturn("DeviceMessageFileImplReadTest");
        when(this.path.getFileSystem()).thenReturn(this.fileSystem);
        when(this.path.getFileName()).thenReturn(this.filePart);
        when(this.pathIS.available()).thenReturn(0);
        when(this.pathIS.read()).thenReturn(-1);
        when(this.pathIS.read(any(byte[].class))).thenReturn(-1);
        when(this.pathIS.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        when(this.fileSystemProvider.newInputStream(this.path)).thenReturn(this.pathIS);
    }

    @Test
    public void getName() {
        DeviceMessageFileImpl deviceMessageFile = this.getTestInstance();

        // Business method
        String name = deviceMessageFile.getName();

        // Asserts
        assertThat(name).isEqualTo("DeviceMessageFileImplReadTest");
    }

    @Test
    public void readWithActuallyPassesTheInputStream() throws IOException {
        DeviceMessageFileImpl deviceMessageFile = this.getTestInstance();

        // Business method
        deviceMessageFile.readWith(this::read);

        // Asserts
        assertThat(this.readCalled).isTrue();
    }

    @Test
    public void readWithClosesTheInputStream() throws IOException {
        DeviceMessageFileImpl deviceMessageFile = this.getTestInstance();

        // Business method
        deviceMessageFile.readWith(this::read);

        // Asserts
        verify(pathIS).close();
    }

    private DeviceMessageFileImpl getTestInstance() {
        return new DeviceMessageFileImpl(null).init(this.deviceType, this.path);
    }

    private void read(InputStream inputStream) {
        this.readCalled = true;
    }

}