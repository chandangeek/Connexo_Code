package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dlms.DlmsSession;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.prime.PrimeClock;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 14:32
 */
public class ClockControl extends PrimeMessageExecutor {

    private static final String SETTIMEZONE = "SetTimeZone";

    public ClockControl(DlmsSession session) {
        super(session);
    }

    public static MessageCategorySpec getCategorySpec() {
        MessageCategorySpec spec = new MessageCategorySpec("Clock control");

        spec.addMessageSpec(addBasicMsgWithAttributes("Set clock time zone", SETTIMEZONE, true, "GMT offset (in hours)"));

        return spec;
    }

    public boolean canHandle(MessageEntry messageEntry) {
        return isMessageTag(SETTIMEZONE, messageEntry);
    }

    public final MessageResult execute(MessageEntry messageEntry) throws IOException {
        try {

            if (isMessageTag(SETTIMEZONE, messageEntry)) {
                return writeClockTimeZone(messageEntry);
            }

        } catch (IOException e) {
            getLogger().severe("An error occured while handling message [" + messageEntry.getContent() + "]: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "] in [" + getClass().getSimpleName() + "]");
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult writeClockTimeZone(MessageEntry messageEntry) throws IOException {
        getLogger().info("Setting device clock timezone");
        String[] parts = messageEntry.getContent().split("=");
        int offset;
        try {
            offset = Integer.valueOf(parts[1].substring(1).split("\"")[0]);
        } catch (NumberFormatException e) {
            getLogger().info("Invalid number for clock timezone: " + parts[1].substring(1).split("\"")[0] + ". Could not execute message.");
            return MessageResult.createFailed(messageEntry);
        }
        getSession().getCosemObjectFactory().getClock(PrimeClock.CLOCK_OBIS_CODE).setTimeZone(offset * -1 * 60);
        getLogger().info("Device clock timezone was set successfully");
        return MessageResult.createSuccess(messageEntry);
    }
}