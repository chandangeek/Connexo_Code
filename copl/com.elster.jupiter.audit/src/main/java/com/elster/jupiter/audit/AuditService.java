/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AuditService {
    String COMPONENTNAME = "ADT";

    Finder<AuditTrail> getAuditTrail(AuditTrailFilter filter);

    AuditTrailFilter newAuditTrailFilter(ApplicationType applicationType);

}
