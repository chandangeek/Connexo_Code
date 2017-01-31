/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a thesaurus that is especially designed to provide
 * translation information for privileges.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-30 (13:18)
 */
@ProviderType
public interface PrivilegeThesaurus {
    String translateComponentName(String privilegeKey);
    String translateResourceName(String privilegeKey);
    String translatePrivilegeKey(String privilegeKey);
}