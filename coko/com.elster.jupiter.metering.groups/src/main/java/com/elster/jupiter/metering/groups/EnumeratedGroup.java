/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface EnumeratedGroup<T extends HasId & IdentifiedObject> extends Group<T> {

    void endMembership(T member, Instant when);

    interface Entry<T extends HasId & IdentifiedObject> extends Effectivity {
        T getMember();

        EnumeratedGroup<T> getGroup();
    }

    Entry<T> add(T member, Range<Instant> range);

    void remove(Entry<T> entry);

    List<? extends Entry<T>> getEntries();

}
