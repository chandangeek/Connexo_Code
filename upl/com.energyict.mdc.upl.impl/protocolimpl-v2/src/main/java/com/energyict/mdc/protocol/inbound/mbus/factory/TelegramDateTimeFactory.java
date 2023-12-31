/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;

import java.time.Instant;
import java.util.Optional;

public class TelegramDateTimeFactory {

    public static Instant from(Telegram telegram, InboundContext inboundContext) {
        if (telegram.getBody().getBodyPayload().getRecords().size() > 2){
            Optional<Instant> parsedTime = telegram.getBody().getBodyPayload().getRecords().get(2).getDataField().getTimeValue();
            if (parsedTime.isPresent()) {
                inboundContext.getLogger().info("Proper telegram date-time extracted: " + parsedTime.get());
                return parsedTime.get();
            } else {
                String dateTime = telegram.getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue();
                inboundContext.getLogger().info("Telegram date-time not extracted, doing our best to parse: " + dateTime);
                try {
                    return Instant.parse(dateTime);
                } catch (Exception ex) {
                    inboundContext.getLogger().info("Could not parse " + dateTime + " to instant: " + ex.getMessage());
                }
            }
        } else {
            inboundContext.getLogger().info("Telegram date-time field not present");
        }

        // fallback
        return Instant.now();
    }
}
