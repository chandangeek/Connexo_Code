package com.elster.jupiter.cbo;

import static java.util.Objects.requireNonNull;

public final class EndDeviceEventTypeCodeBuilder {
    private EndDeviceType type = EndDeviceType.NA;
    private EndDeviceDomain domain = EndDeviceDomain.NA;
    private EndDeviceSubDomain subDomain = EndDeviceSubDomain.NA;
    private EndDeviceEventorAction eventOrAction = EndDeviceEventorAction.NA;

    private EndDeviceEventTypeCodeBuilder(EndDeviceType type) {
        this.type = type;
    }

    public static EndDeviceEventTypeCodeBuilder type(EndDeviceType type) {
        return new EndDeviceEventTypeCodeBuilder(requireNonNull(type));
    }

    public EndDeviceEventTypeCodeBuilder domain(EndDeviceDomain domain) {
        this.domain = requireNonNull(domain);
        return this;
    }

    public EndDeviceEventTypeCodeBuilder subDomain(EndDeviceSubDomain subDomain) {
        this.subDomain = requireNonNull(subDomain);
        return this;
    }

    public EndDeviceEventTypeCodeBuilder eventOrAction(EndDeviceEventorAction eventOrAction) {
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
