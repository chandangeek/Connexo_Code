package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UsagePointAlreadyLinkedToAnotherDeviceException extends LocalizedException {

    private final MeterActivation meterActivation;

    public UsagePointAlreadyLinkedToAnotherDeviceException(Thesaurus thesaurus, DateTimeFormatter formatter, MeterActivation meterActivation) {
        super(thesaurus, getMessageSeed(meterActivation), getMessageArgs(meterActivation, formatter));
        this.meterActivation = meterActivation;
    }

    public MeterActivation getMeterActivation() {
        return meterActivation;
    }

    private static MessageSeed getMessageSeed(MeterActivation meterActivation) {
        return meterActivation.getEnd() == null ?
                MessageSeeds.USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE : MessageSeeds.USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE_UNTIL;
    }

    private static String[] getMessageArgs(MeterActivation meterActivation, DateTimeFormatter formatter) {
        List<String> args = new ArrayList<>(3);
        args.add(meterActivation.getMeter().map(EndDevice::getName).orElse(null));
        args.add(getFormattedInstant(formatter, meterActivation.getStart()));
        if (meterActivation.getEnd() != null) {
            args.add(getFormattedInstant(formatter, meterActivation.getEnd()));
        }
        return args.stream().toArray(String[]::new);
    }

    private static String getFormattedInstant(DateTimeFormatter formatter, Instant time) {
        return formatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }
}
