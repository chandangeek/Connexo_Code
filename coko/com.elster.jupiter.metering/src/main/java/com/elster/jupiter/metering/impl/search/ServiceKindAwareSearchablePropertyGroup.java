package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.search.SearchablePropertyGroup;

public abstract class ServiceKindAwareSearchablePropertyGroup implements SearchablePropertyGroup {

    public abstract ServiceKind getServiceKind();
}
