/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.location;

import aQute.bnd.annotation.ConsumerType;

import java.util.Map;

@ConsumerType
public interface SearchLocationService {

    Map<Long, String> findLocations(String locationPart);

}
