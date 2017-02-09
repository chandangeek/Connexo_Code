/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface Location {

    long getId();
    List<? extends LocationMember> getMembers();
    Optional<LocationMember> getMember(String locale);

    List<List<String>> format();
}
