/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import java.util.Map;

public interface FsmUsagePointProvider {

    boolean areProcessPropertiesAvailableForUP(Map<String, Object> processProperties, long id);

    String getDeviceMRID(long id);

    String getUsagePointMRID(long id);

}
