package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;

public class EvtSubscribeCmdPayload extends LittleEndianData {
    public static final int SIZE = 5;

    /**
     * Bitfield giving set of events that the Subscriber is interested in
     */
    private final int events;

    /**
     * Indicates type of Cmd Signature wanted in the Event Publish
     */
    private final SecurityScheme publishSecScheme;

    public EvtSubscribeCmdPayload(int events, SecurityScheme publishSecScheme) {
        super(SIZE);
        this.events = events;
        this.publishSecScheme = publishSecScheme;
        getRawBuffer().putInt(events).put((byte)publishSecScheme.ordinal());
    }

    public EvtSubscribeCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        events = getRawBuffer().getInt();

        int scheme = getRawBuffer().get();
        SecurityScheme[] securitySchemeValues = SecurityScheme.values();
        if (scheme < securitySchemeValues.length) {
            publishSecScheme = SecurityScheme.fromId(scheme);
        } else {
            // todo change
            throw new java.security.InvalidParameterException(
                    "Invalid security scheme provided=" + scheme +
                            ", valid range=[" + securitySchemeValues[0].ordinal() +
                            "-" + securitySchemeValues[securitySchemeValues.length - 1].ordinal() + "]");
        }
    }

    public int getEvents() {
        return events;
    }

    public SecurityScheme getPublishSecScheme() {
        return publishSecScheme;
    }
}
