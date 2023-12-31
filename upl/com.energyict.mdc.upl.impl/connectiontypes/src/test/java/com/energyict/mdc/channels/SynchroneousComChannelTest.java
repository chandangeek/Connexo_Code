/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels;

import com.energyict.mdc.channel.SynchroneousComChannel;
import com.energyict.mdc.protocol.ComChannelType;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SynchroneousComChannel} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (10:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class SynchroneousComChannelTest {

    private static final int BYTE_VALUE = 31;
    private static final int BUFFER_SIZE = 64;
    @Mock
    private InputStream inputStream;
    @Mock
    private OutputStream outputStream;

    @Before
    public void initializeMocks() {

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateIsWriteMode() {
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.read();

        // Expected a UnsupportedOperationException because newly created SynchroneousComChannel are in write mode and reading and writing is not supported
    }

    @Test
    public void testSwitchToWriteModeWhenAlreadyInWriteMode() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.startWriting();
        comChannel.write(BYTE_VALUE);

        // Asserts
        verify(this.outputStream).write(BYTE_VALUE);
    }

    @Test
    public void testStartWritingReturnsFalseWhenAlreadyInWritingMode() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        boolean changed = comChannel.startWriting();

        // Asserts
        assertThat(changed).isFalse();
    }

    @Test
    public void testStartReadingReturnsFalseWhenAlreadyInReadingMode() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        boolean changed = comChannel.startReading();

        // Asserts
        assertThat(changed).isFalse();
    }

    @Test
    public void testWrite() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.write(BYTE_VALUE);

        // Asserts
        verify(this.outputStream).write(BYTE_VALUE);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testWriteWithIOException() throws IOException {
        doThrow(IOException.class).when(this.outputStream).write(BYTE_VALUE);
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.write(BYTE_VALUE);

        // Was expecting a ConnectionCommunicationException because the OutputStream caused an IOException
    }

    @Test
    public void testWriteIntoBuffer() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();
        byte[] bytes = "testWriteFromBuffer".getBytes();

        // Business method
        comChannel.write(bytes);

        // Asserts
        verify(this.outputStream).write(bytes);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testWriteIntoBufferWithIOException() throws IOException {
        SynchroneousComChannel comChannel = this.newComChannel();
        byte[] bytes = "testWriteFromBuffer".getBytes();
        doThrow(IOException.class).when(this.outputStream).write(bytes);

        // Business method
        comChannel.write(bytes);

        // Was expecting a ConnectionCommunicationException because the OutputStream caused an IOException
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testStartReadingWhenFlushThrowsException() throws IOException {
        doThrow(IOException.class).when(this.outputStream).flush();
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.startReading();

        // Was expecting a ConnectionCommunicationException because the flush caused an IOException
    }

    @Test
    public void testAvailable() throws IOException {
        int expectedAvailableBytes = BUFFER_SIZE;
        when(this.inputStream.available()).thenReturn(expectedAvailableBytes);
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        int availableBytes = comChannel.available();

        // Asserts
        verify(this.inputStream).available();
        assertThat(availableBytes).isEqualTo(expectedAvailableBytes);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testAvailableWithIOException() throws IOException {
        when(this.inputStream.available()).thenThrow(new IOException("For unit testing purposes only"));
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        comChannel.available();

        // Expected a ConnectionCommunicationException because the InputStream cause an IOException
    }

    @Test
    public void testRead() throws IOException {
        when(this.inputStream.read()).thenReturn(BYTE_VALUE);
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        int valueRead = comChannel.read();

        // Asserts
        verify(this.inputStream).read();
        assertThat(valueRead).isEqualTo(BYTE_VALUE);
    }

    @Test
    public void testSwithToReadModeWhenAlreadyInReadMode() throws IOException {
        when(this.inputStream.read()).thenReturn(BYTE_VALUE);
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();
        int valueRead = comChannel.read();

        // Business method
        comChannel.startReading();

        // Asserts
        verify(this.inputStream).read();
        assertThat(valueRead).isEqualTo(BYTE_VALUE);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testReadWithIOException() throws IOException {
        when(this.inputStream.read()).thenThrow(new IOException("For unit testing purposes only"));
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();
        comChannel.read();

        // Expected a ConnectionCommunicationException because the InputStream caused an IOException
    }

    @Test
    public void testReadIntoBuffer() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        when(this.inputStream.read(buffer)).thenReturn(BUFFER_SIZE);
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        int bytesRead = comChannel.read(buffer);

        // Asserts
        verify(this.inputStream).read(buffer);
        assertThat(bytesRead).isEqualTo(BUFFER_SIZE);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testReadIntoBufferWithIOException() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        when(this.inputStream.read(buffer)).thenThrow(new IOException("For unit testing purposes only"));
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        comChannel.read(buffer);

        // Expected a ConnectionCommunicationException because the InputStream caused an IOException
    }

    @Test
    public void testReadIntoBufferWithOffsetAndLength() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesWanted = BUFFER_SIZE / 2;
        int expectedBytesRead = BUFFER_SIZE / 3;
        when(this.inputStream.read(buffer, 0, bytesWanted)).thenReturn(expectedBytesRead);
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        int bytesRead = comChannel.read(buffer, 0, bytesWanted);

        // Asserts
        verify(this.inputStream).read(buffer, 0, bytesWanted);
        assertThat(bytesRead).isEqualTo(expectedBytesRead);
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testReadIntoBufferWithOffsetAndLengthWithIOException() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesWanted = BUFFER_SIZE / 2;
        when(this.inputStream.read(buffer, 0, bytesWanted)).thenThrow(new IOException("For unit testing purposes only"));
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        comChannel.read(buffer, 0, bytesWanted);

        // Expected a ConnectionCommunicationException because the InputStream caused an IOException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteWhileReading() {
        SynchroneousComChannel comChannel = this.newComChannel();
        comChannel.startReading();

        // Business method
        comChannel.write(BYTE_VALUE);

        // Expected a UnsupportedOperationException because the ComChannel is actually in read mode
    }

    @Test
    public void testClose() {
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.close();

        // Nothing to assert but should not cause any exceptions.
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testCloseWithIOExceptionOnInputStream() throws IOException {
        doThrow(IOException.class).when(this.inputStream).close();
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.close();

        // Expected a ConnectionCommunicationException because the InputStream caused an IOException when closing
    }

    @Test(expected = ConnectionCommunicationException.class)
    public void testCloseWithIOExceptionOnOutputStream() throws IOException {
        doThrow(IOException.class).when(this.outputStream).close();
        SynchroneousComChannel comChannel = this.newComChannel();

        // Business method
        comChannel.close();

        // Expected a ConnectionCommunicationException because the OutputStream caused an IOException when closoutg
    }

    private SynchroneousComChannel newComChannel() {
        return new SynchroneousComChannel(this.inputStream, this.outputStream) {
            @Override
            public ComChannelType getComChannelType() {
                return ComChannelType.SocketComChannel;
            }
        };
    }

}