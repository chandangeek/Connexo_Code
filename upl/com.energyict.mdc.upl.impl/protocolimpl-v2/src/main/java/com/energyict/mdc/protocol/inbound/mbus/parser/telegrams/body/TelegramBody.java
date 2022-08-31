package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import java.util.Arrays;

public class  TelegramBody {

    private TelegramBodyHeader bodyHeader;
    private TelegramBodyPayload bodyPayload;

    public TelegramBody() {
        this.bodyHeader = new TelegramBodyHeader();
        this.bodyPayload = new TelegramBodyPayload();
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

    public void debugOutput() {
        if(this.bodyHeader != null) {
            this.bodyHeader.debugOutput();
        }
        if(this.bodyPayload != null) {
            this.bodyPayload.debugOutput();
        }
    }
}