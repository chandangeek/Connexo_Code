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
 * Tests the {@link DeviceMessageFileByteContentConsumer} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileByteContentConsumerTest {

    @Test
    public void numberOfBytesIsMultipleOfBufferSize() {
        int bufferSize = 5;
        byte[] expectedBytes = Strings.repeat(Strings.repeat("X", 5), 10).getBytes();
        DeviceMessageFileByteContentConsumer testInstance = this.getTestInstance(bufferSize);

        // Business method
        testInstance.accept(new ByteArrayInputStream(expectedBytes));

        // Asserts
        assertThat(testInstance.getBytes()).isEqualTo(expectedBytes);
    }

    @Test
    public void numberOfBytesIsNotMultipleOfBufferSize() {
        int bufferSize = 5;
        byte[] expectedBytes = Strings.repeat("XyX", 11).getBytes();
        DeviceMessageFileByteContentConsumer testInstance = this.getTestInstance(bufferSize);

        // Business method
        testInstance.accept(new ByteArrayInputStream(expectedBytes));

        // Asserts
        assertThat(testInstance.getBytes()).isEqualTo(expectedBytes);
    }

    @Test
    public void numberOfBytesSmallerThanBufferSize() {
        byte[] expectedBytes = Strings.repeat("X", 100).getBytes();
        DeviceMessageFileByteContentConsumer testInstance = this.getTestInstance();

        // Business method
        testInstance.accept(new ByteArrayInputStream(expectedBytes));

        // Asserts
        assertThat(testInstance.getBytes()).isEqualTo(expectedBytes);
    }

    @Test(expected = UnderlyingIOException.class)
    public void ioExceptionIsWrapped() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        doThrow(IOException.class).when(inputStream).read();
        doThrow(IOException.class).when(inputStream).read(any(byte[].class));
        doThrow(IOException.class).when(inputStream).read(any(byte[].class), anyInt(), anyInt());
        DeviceMessageFileByteContentConsumer testInstance = this.getTestInstance();

        // Business method
        testInstance.accept(inputStream);

        // Asserts: see expected exception rule
    }

    private DeviceMessageFileByteContentConsumer getTestInstance() {
        return new DeviceMessageFileByteContentConsumer();
    }

    private DeviceMessageFileByteContentConsumer getTestInstance(int bufferSize) {
        return new DeviceMessageFileByteContentConsumer(bufferSize);
    }

}