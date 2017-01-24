package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.SchedulingInterval;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SchedulingConfigurationEvent extends AbstractField<SchedulingConfigurationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int COMMAND_NR_OF_BYTES = 1;

    private int command;
    private DateTime schedulingDateTime;
    private SchedulingInterval schedulingInterval;
    private PaddingData paddingData;

    public SchedulingConfigurationEvent(Clock clock, TimeZone timeZone) {
        this.command = 0;
        this.schedulingDateTime = new DateTime(clock, timeZone);
        this.schedulingInterval = new SchedulingInterval();
        this.paddingData = new PaddingData(5);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getBytesFromInt(command, COMMAND_NR_OF_BYTES),
                schedulingDateTime.getBytes(),
                schedulingInterval.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public SchedulingConfigurationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.command = getIntFromBytes(rawData, ptr, COMMAND_NR_OF_BYTES);
        ptr += COMMAND_NR_OF_BYTES;

        this.schedulingDateTime.parse(rawData, ptr);
        ptr += schedulingDateTime.getLength();

        this.schedulingInterval.parse(rawData, ptr);
        ptr += schedulingInterval.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return "Schedule config has changed: " +
                "Command: " + command +
                " - Start date: " + schedulingDateTime.getDate() +
                " - Interval: " + schedulingInterval.toString();
    }

    public int getCommand() {
        return command;
    }

    public DateTime getSchedulingDateTime() {
        return schedulingDateTime;
    }

    public SchedulingInterval getSchedulingInterval() {
        return schedulingInterval;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}