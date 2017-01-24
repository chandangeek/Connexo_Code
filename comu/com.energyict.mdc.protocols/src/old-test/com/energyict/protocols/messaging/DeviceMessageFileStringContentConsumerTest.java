package com.energyict.protocols.messaging;

import com.elster.jupiter.orm.UnderlyingIOException;

import com.google.common.base.Strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceMessageFileStringContentConsumer} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileStringContentConsumerTest {

    @Test
    public void numberOfBytesIsMultipleOfBufferSize() {
        int bufferSize = 5;
        String expected = Strings.repeat(Strings.repeat("X", 5), 10);
        DeviceMessageFileStringContentConsumer testInstance = this.getTestInstance(bufferSize);

        // Business method
        testInstance.accept(new ByteArrayInputStream(expected.getBytes()));

        // Asserts
        assertThat(testInstance.getContents()).isEqualTo(expected);
    }

    @Test
    public void numberOfBytesIsNotMultipleOfBufferSize() {
        int bufferSize = 5;
        String expected = Strings.repeat("XyX", 11);
        DeviceMessageFileStringContentConsumer testInstance = this.getTestInstance(bufferSize);

        // Business method
        testInstance.accept(new ByteArrayInputStream(expected.getBytes()));

        // Asserts
        assertThat(testInstance.getContents()).isEqualTo(expected);
    }

    @Test
    public void numberOfBytesSmallerThanBufferSize() {
        String expected = Strings.repeat("X", 100);
        DeviceMessageFileStringContentConsumer testInstance = this.getTestInstance();

        // Business method
        testInstance.accept(new ByteArrayInputStream(expected.getBytes()));

        // Asserts
        assertThat(testInstance.getContents()).isEqualTo(expected);
    }

    @Test(expected = UnderlyingIOException.class)
    public void ioExceptionIsWrapped() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream).read();
        doThrow(IOException.class).when(inputStream).read(any(byte[].class));
        doThrow(IOException.class).when(inputStream).read(any(byte[].class), anyInt(), anyInt());
        DeviceMessageFileStringContentConsumer testInstance = this.getTestInstance();

        // Business method
        testInstance.accept(inputStream);

        // Asserts: see expected exception rule
    }

    private DeviceMessageFileStringContentConsumer getTestInstance() {
        return new DeviceMessageFileStringContentConsumer();
    }

    private DeviceMessageFileStringContentConsumer getTestInstance(int bufferSize) {
        return new DeviceMessageFileStringContentConsumer(bufferSize);
    }

}