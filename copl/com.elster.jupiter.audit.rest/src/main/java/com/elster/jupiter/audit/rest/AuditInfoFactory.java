/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.nls.Thesaurus;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface AuditInfoFactory {
    AuditInfo from(AuditTrail audit, Thesaurus thesaurus);
}
