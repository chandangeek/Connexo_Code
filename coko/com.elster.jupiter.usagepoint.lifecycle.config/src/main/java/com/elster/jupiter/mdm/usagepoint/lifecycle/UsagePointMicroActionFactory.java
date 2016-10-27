package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface UsagePointMicroActionFactory {

    MicroAction from(MicroAction.Key key);
}
