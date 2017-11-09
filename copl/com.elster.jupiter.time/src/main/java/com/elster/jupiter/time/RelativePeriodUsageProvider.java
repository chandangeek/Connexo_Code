package com.elster.jupiter.time;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface RelativePeriodUsageProvider {

    List<RelativePeriodUsageInfo> getUsageReferences(long relativePeriodId);

    String getType();
}
