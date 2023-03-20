/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;

import java.util.Arrays;
import java.util.StringJoiner;

public class  TelegramBody {

    private final MerlinLogger logger;
    private TelegramBodyHeader bodyHeader;
    private TelegramBodyPayload bodyPayload;

    public TelegramBody(MerlinLogger logger) {
        this.logger = logger;
        this.bodyHeader = new TelegramBodyHeader(logger);
        this.bodyPayload = new TelegramBodyPayload(logger);
    }

    public TelegramBodyHeader getBodyHeader() {
        return bodyHeader;
    }

    public void setBodyHeader(TelegramBodyHeader bodyHeader) {
        this.bodyHeader = bodyHeader;
    }

    public TelegramBodyPayload getBodyPayload() {
        return bodyPayload;
    }

    public void setBodyPayload(TelegramBodyPayload bodyPayload) {
        this.bodyPayload = bodyPayload;
    }

    public void createTelegramBody(String[] body) {
        // first extract header
        this.bodyHeader.createTelegramBodyHeader(Arrays.copyOfRange(body, 0, 5));
        this.bodyPayload.createTelegramBodyPayload(Arrays.copyOfRange(body, 5, body.length));
    }

    public void parse() {
        this.bodyPayload.parse();
    }

    public String getPowerValue() {
        return this.getBodyPayload().getRecords().get(5).getDataField().getParsedValue();
    }

    public String getEnergyValue() {
        return this.getBodyPayload().getRecords().get(3).getDataField().getParsedValue();
    }

    public void debugOutput(StringJoiner joiner) {
        if (this.bodyHeader != null) {
            this.bodyHeader.debugOutput(joiner);
        }
        if (this.bodyPayload != null) {
            this.bodyPayload.debugOutput(joiner);
        }
    }
}