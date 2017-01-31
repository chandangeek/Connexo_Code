/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface GroupBuilder<T extends HasId & IdentifiedObject, EG extends EnumeratedGroup<T>, QG extends QueryGroup<T>> {

    EnumeratedGroupBuilder<T, EG> containing(T... moreElements);

    QueryGroupBuilder<T, QG> withConditions(SearchablePropertyValue... conditions);

    interface GroupCreator<G extends Group<?>> {
        G create();

        G validate();
    }

    interface EnumeratedGroupBuilder<T extends HasId & IdentifiedObject, G extends EnumeratedGroup<T>>
            extends GroupCreator<G> {
        EnumeratedGroupBuilder<T, G> at(Instant at);
        EnumeratedGroupBuilder<T, G> containing(T... moreDevices);

        EnumeratedGroupBuilder<T, G> setName(String name);
        EnumeratedGroupBuilder<T, G> setMRID(String mRID);
        EnumeratedGroupBuilder<T, G> setDescription(String description);
        EnumeratedGroupBuilder<T, G> setAliasName(String aliasName);
        EnumeratedGroupBuilder<T, G> setType(String type);
        EnumeratedGroupBuilder<T, G> setLabel(String label);
    }

    interface QueryGroupBuilder<T extends HasId & IdentifiedObject, G extends QueryGroup<T>>
            extends GroupCreator<G> {
        QueryGroupBuilder<T, G> withConditions(SearchablePropertyValue... conditions);
        QueryGroupBuilder<T, G> setQueryProviderName(String queryProviderName);
        QueryGroupBuilder<T, G> setSearchDomain(SearchDomain searchDomain);

        QueryGroupBuilder<T, G> setName(String name);
        QueryGroupBuilder<T, G> setMRID(String mRID);
        QueryGroupBuilder<T, G> setDescription(String description);
        QueryGroupBuilder<T, G> setAliasName(String aliasName);
        QueryGroupBuilder<T, G> setType(String type);
        QueryGroupBuilder<T, G> setLabel(String label);
    }
}
