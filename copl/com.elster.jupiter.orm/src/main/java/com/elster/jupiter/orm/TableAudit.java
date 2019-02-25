/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

public interface TableAudit {

    Table<?> getTable();

    Integer getDomainContext();

    List<Object> getDomainPkValues(Object object);

    List<Object> getContextPkValues(Object object);

    List<Object> getResersePkValues(Object object);

    Optional<Long> getReverseReferenceMap(Object object);

    Table getTouchTable();

    @ProviderType
    interface Builder {

        Builder domainContext(Integer domainContext);

        Builder domainReferences(String... foreignKeyConstraints);

        Builder domainReferenceColumn(String domainReferenceColumn);

        Builder contextReferenceColumn(String... contextReferenceColumns);

        Builder reverseReferenceMap(String reverseReferenceMap);

        TableAudit build();
    }

}
