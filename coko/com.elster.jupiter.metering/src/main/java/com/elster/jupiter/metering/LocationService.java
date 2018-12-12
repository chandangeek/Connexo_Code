/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Optional;

public interface LocationService {
    String COMPONENT_NAME = "LOC";

    Optional<Location> findLocationById(long id);

}
