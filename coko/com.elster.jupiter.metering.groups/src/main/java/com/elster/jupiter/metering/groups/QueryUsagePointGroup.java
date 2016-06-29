package com.elster.jupiter.metering.groups;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface QueryUsagePointGroup extends UsagePointGroup {
    String TYPE_IDENTIFIER = "QUG";
}