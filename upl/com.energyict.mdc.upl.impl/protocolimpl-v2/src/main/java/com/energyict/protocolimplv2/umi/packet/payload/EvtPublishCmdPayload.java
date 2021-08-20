package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.concurrent.TimeUnit;

public class EvtPublishCmdPayload extends LittleEndianData {
    public static final int SIZE = 10;

    /**
     * Bitfield giving the set of events that caused the Event Publish.
     */
    private final int events; // 4 bytes

    /**
     * Publisher’s datetime.
     */
    private final long datetime; // 4 bytes

    /**
     * The milliseconds part of the Publisher’s datetime.
     */
    private final int milliseconds; // 2 byte

    public EvtPublishCmdPayload(int events) {
        super(SIZE);
        this.events = events;
        long currentMillis = System.currentTimeMillis();
        datetime = TimeUnit.MILLISECONDS.toSeconds(currentMillis);
        milliseconds = (int) (currentMillis - TimeUnit.SECONDS.toMillis(datetime));
        getRawBuffer().putInt(events).putInt((int) datetime).putShort((short) milliseconds);
    }

    public EvtPublishCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        events = getRawBuffer().getInt();
        datetime = getRawBuffer().getInt();
        milliseconds = getRawBuffer().getShort();
    }

    public int getEvents() {
        return events;
    }

    public long getDatetime() {
        return datetime;
    }

    public int getMilliseconds() {
        return milliseconds;
    }
}
