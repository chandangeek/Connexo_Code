package com.elster.jupiter.metering.groups;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.RangeSet;

import java.time.Instant;

@ProviderType
public interface Membership<T> {

    RangeSet<Instant> getRanges();

    T getMember();
}
