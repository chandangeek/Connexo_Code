/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import java.time.Instant;

/**
 * Serves as the super class for classes that will implement
 * the {@link PersistentDomainExtension} interface to make sure that
 * all the required fields as described by {@link HardCodedFieldNames}
 * are correctly provided to the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-12 (13:23)
 */
public abstract class AbstractPersistentDomainExtension {
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    protected RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    protected void setRegisteredCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
    }

}