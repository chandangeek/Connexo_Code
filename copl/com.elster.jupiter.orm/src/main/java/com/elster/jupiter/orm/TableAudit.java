/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

public interface TableAudit {

    Table<?> getTable();

    String getDomain();

    String getContext();

    List<Object> getPkColumns(Object object);

    String getDomainReferences(Object object);

    Object getDomainShortReference(Object object);

    String getContextReferences(Object object);

    List<String> getReferences(Object object);

    String getObjectIndentifier(Object object);

    Table getTouchTable();

    @ProviderType
    interface Builder {

        Builder domain(String domain);

        Builder context(String context);

        Builder touchDomain(String domainForeignKey);

        Builder touchContext(String contextForeignKey);

        Builder references(ForeignKeyConstraint foreignKeyConstraint);

        Builder references(String... foreignKeyConstraints);

        TableAudit build();
    }

}
