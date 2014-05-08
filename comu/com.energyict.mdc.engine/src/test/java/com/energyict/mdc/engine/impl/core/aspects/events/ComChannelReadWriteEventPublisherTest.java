package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.comserver.core.ConfigurableReadComChannel;
import com.energyict.comserver.core.SystemOutComChannel;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.aspects.events.ComChannelReadWriteEventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.io.ReadEvent;
import com.energyict.mdc.engine.impl.events.io.WriteEvent;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests the pointcuts and advice defined in {@link ComChannelReadWriteEventPublisher}.
 * Uses a {@link SystemOutComChannel} to read and write from.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-13 (15:16)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComChannelReadWriteEventPublisherTest {

    private static final int singleByte = 97;   // I just like prime numbers
    private static final byte[] FIRST_SERIES_OF_BYTES = "first series of bytes".getBytes();
    private static final byte[] SECOND_SERIES_OF_BYTES = "ComChannelReadWriteEventPublisherTest".getBytes();
    private static final int SECOND_SERIES_OF_BYTES_OFFSET = 3;
    private static final int SECOND_SERIES_OF_BYTES_LENGTH = SECOND_SERIES_OF_BYTES.length - SECOND_SERIES_OF_BYTES_OFFSET;

    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComPort comPort;
    private byte[] expectedBytes;

    @Before
    public void initializeMocksAndFactories () throws IOException {
        this.initializeEventPublisher();
        this.initializeExpectedBytes();
    }

    private void initializeEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    private void initializeExpectedBytes () throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(singleByte);
        os.write(FIRST_SERIES_OF_BYTES);
        os.write(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        this.expectedBytes = os.toByteArray();
    }

    @Test
    public void testReadBytesBetweenCreationAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(ReadEvent.class);
        ReadEvent readEvent = (ReadEvent) event;
        assertThat(readEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(readEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testReadBytesBetweenCreationAndStartWriting () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(ReadEvent.class);
        ReadEvent readEvent = (ReadEvent) event;
        assertThat(readEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(readEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testReadBytesBetweenStartReadingAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(ReadEvent.class);
        ReadEvent readEvent = (ReadEvent) event;
        assertThat(readEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(readEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testReadBytesBetweenStartReadingAndStartWriting () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(ReadEvent.class);
        ReadEvent readEvent = (ReadEvent) event;
        assertThat(readEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(readEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testWriteBytesBetweenCreationAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(WriteEvent.class);
        WriteEvent writeEvent = (WriteEvent) event;
        assertThat(writeEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(writeEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testWriteBytesBetweenCreationAndStartReading () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the writing session
        comChannel.startReading();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(WriteEvent.class);
        WriteEvent writeEvent = (WriteEvent) event;
        assertThat(writeEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(writeEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(WriteEvent.class);
        WriteEvent writeEvent = (WriteEvent) event;
        assertThat(writeEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(writeEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndStartReading () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.startReading();

        // Asserts
        ArgumentCaptor<ComServerEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ComServerEvent.class);
        verify(this.eventPublisher).publish(eventArgumentCaptor.capture());
        ComServerEvent event = eventArgumentCaptor.getValue();
        assertThat(event).isInstanceOf(WriteEvent.class);
        WriteEvent writeEvent = (WriteEvent) event;
        assertThat(writeEvent.getComPort()).isEqualTo(this.comPort);
        assertThat(writeEvent.getBytes()).isEqualTo(this.expectedBytes);
    }

    private void readFrom (ComPortRelatedComChannel comChannel) {
        this.readSingleByte(comChannel);
        this.readBytes(comChannel);
        this.readBytesWithOffset(comChannel);
    }

    private void readSingleByte (ComPortRelatedComChannel comChannel) {
        comChannel.read();
    }

    private void readBytes (ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[FIRST_SERIES_OF_BYTES.length];
        comChannel.read(bytes);
    }

    private void readBytesWithOffset (ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[SECOND_SERIES_OF_BYTES.length];
        comChannel.read(bytes, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
    }

    private ComPortRelatedComChannel newComChannelForReading () {
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        comChannel.whenRead(singleByte);
        comChannel.whenReadFromBuffer(FIRST_SERIES_OF_BYTES);
        comChannel.whenReadFromBufferWithOffset(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        comChannel.setComPort(this.comPort);
        return comChannel;
    }

    private void writeTo (ComPortRelatedComChannel comChannel) {
        this.writeSingleByteTo(comChannel);
        this.writeBytesTo(comChannel);
    }

    private void writeSingleByteTo (ComPortRelatedComChannel comChannel) {
        comChannel.write(singleByte);
    }

    private void writeBytesTo (ComPortRelatedComChannel comChannel) {
        comChannel.write(FIRST_SERIES_OF_BYTES);
        byte[] secondSeriesOfBytes = new byte[SECOND_SERIES_OF_BYTES_LENGTH];
        System.arraycopy(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, secondSeriesOfBytes, 0, SECOND_SERIES_OF_BYTES_LENGTH);
        comChannel.write(secondSeriesOfBytes);
    }

    private ComPortRelatedComChannel newComChannelForWriting () {
        SystemOutComChannel comChannel = new SystemOutComChannel();
        comChannel.setComPort(this.comPort);
        return comChannel;
    }

}