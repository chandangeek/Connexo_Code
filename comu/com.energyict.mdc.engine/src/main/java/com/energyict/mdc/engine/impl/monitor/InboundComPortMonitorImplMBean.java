/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import javax.management.openmbean.CompositeData;

public interface InboundComPortMonitorImplMBean {

    CompositeData getOperationalStatisticsCompositeData();

}