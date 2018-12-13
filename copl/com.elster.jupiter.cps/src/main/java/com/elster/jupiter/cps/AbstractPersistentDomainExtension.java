/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.Size;
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
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    @Size(max=80)
    private String userName;

    protected RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    protected void setRegisteredCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
    }

}