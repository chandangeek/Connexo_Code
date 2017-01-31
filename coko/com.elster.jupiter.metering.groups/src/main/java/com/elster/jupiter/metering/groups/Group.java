/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Subquery;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface Group<T extends HasId & IdentifiedObject> extends HasId, IdentifiedObject {

    String getType();

    List<T> getMembers(Instant instant);

    List<T> getMembers(Instant instant, int start, int limit);

    long getMemberCount(Instant instant);

    List<Membership<T>> getMembers(Range<Instant> range);

    boolean isMember(T object, Instant instant);

    void setName(String name);

    void setMRID(String mrid);

    String getLabel();

    void setLabel(String label);

    void setDescription(String description);

    void setAliasName(String aliasName);

    boolean isDynamic();

    void update();

    void delete();

    long getVersion();

    Subquery toSubQuery(String... fields);
}
