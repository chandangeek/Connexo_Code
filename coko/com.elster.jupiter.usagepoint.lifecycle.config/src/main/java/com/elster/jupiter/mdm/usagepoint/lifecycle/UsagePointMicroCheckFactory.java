package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface UsagePointMicroCheckFactory {

    MicroCheck from(MicroCheck.Key key);
}
