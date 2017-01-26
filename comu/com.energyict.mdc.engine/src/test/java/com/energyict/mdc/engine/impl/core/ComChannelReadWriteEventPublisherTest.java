package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.io.ReadEvent;
import com.energyict.mdc.engine.impl.events.io.WriteEvent;
import com.energyict.mdc.protocol.api.services.HexService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Tests that a {@link ComPortRelatedComChannelImpl} publishes
 * events when read from and written to.
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
    @Mock
    private HexService hexService;
    @Mock
    private DeviceMessageService deviceMessageService;

    private Clock clock = Clock.systemDefaultZone();
    private byte[] expectedBytes;

    @Before
    public void initializeMocksAndFactories () throws IOException {
        this.initializeExpectedBytes();
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
        ConfigurableReadComChannel configurableComChannel = new ConfigurableReadComChannel();
        configurableComChannel.whenRead(singleByte);
        configurableComChannel.whenReadFromBuffer(FIRST_SERIES_OF_BYTES);
        configurableComChannel.whenReadFromBufferWithOffset(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        return new ComPortRelatedComChannelImpl(configurableComChannel, this.comPort, this.clock, deviceMessageService, this.hexService, eventPublisher);
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
        SystemOutComChannel systemOutComChannel = new SystemOutComChannel();
        return new ComPortRelatedComChannelImpl(systemOutComChannel, this.comPort, this.clock, deviceMessageService, this.hexService, eventPublisher);
    }

}