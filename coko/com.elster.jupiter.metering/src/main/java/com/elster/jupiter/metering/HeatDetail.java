/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:13
 * To change this template use File | Settings | File Templates.
 */
public interface HeatDetail extends UsagePointDetail {

    Quantity getPhysicalCapacity();

    Quantity getPressure();

    YesNoAnswer isBypassInstalled();

    BypassStatus getBypassStatus();

    YesNoAnswer isValveInstalled();
}
