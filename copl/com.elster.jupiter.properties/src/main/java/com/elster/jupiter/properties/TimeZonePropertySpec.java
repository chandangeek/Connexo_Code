/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.util.TimeZone;

/**
 * @author sva
 * @since 02/02/2015 - 11:38
 */
public interface TimeZonePropertySpec extends PropertySpec {

    @Override
    ValueFactory<TimeZone> getValueFactory();

}