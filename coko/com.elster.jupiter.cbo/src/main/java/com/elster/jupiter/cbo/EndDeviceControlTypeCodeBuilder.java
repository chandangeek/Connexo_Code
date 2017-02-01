/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import static java.util.Objects.requireNonNull;

public class EndDeviceControlTypeCodeBuilder {
    private EndDeviceType type = EndDeviceType.NA;
    private EndDeviceDomain domain = EndDeviceDomain.NA;
    private EndDeviceSubDomain subDomain = EndDeviceSubDomain.NA;
    private EndDeviceEventOrAction eventOrAction = EndDeviceEventOrAction.NA;

    private EndDeviceControlTypeCodeBuilder(EndDeviceType type) {
        this.type = type;
    }

    public static EndDeviceControlTypeCodeBuilder type(EndDeviceType type) {
        return new EndDeviceControlTypeCodeBuilder(requireNonNull(type));
    }

    public EndDeviceControlTypeCodeBuilder domain(EndDeviceDomain domain) {
        this.domain = requireNonNull(domain);
        return this;
    }

    public EndDeviceControlTypeCodeBuilder subDomain(EndDeviceSubDomain subDomain) {
        this.subDomain = requireNonNull(subDomain);
        return this;
    }

    public EndDeviceControlTypeCodeBuilder eventOrAction(EndDeviceEventOrAction eventOrAction) {
        this.eventOrAction = requireNonNull(eventOrAction);
        return this;
    }

    public String toCode() {
        return new StringBuilder().append(type.getValue()).append('.')
                .append(domain.getValue()).append('.')
                .append(subDomain.getValue()).append('.')
                .append(eventOrAction.getValue()).toString();
    }
}
