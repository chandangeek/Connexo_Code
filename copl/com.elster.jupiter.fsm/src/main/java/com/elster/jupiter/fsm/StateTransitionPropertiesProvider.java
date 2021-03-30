/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ConsumerType;

import java.util.Map;

@ConsumerType
public interface StateTransitionPropertiesProvider {

    boolean areProcessPropertiesAvailableForUP(Map<String, Object> processProperties, long id);

    String getMeterMRID(long id);

    String getDeviceMRID(long id);

    String getUsagePointMRID(long id);

}
