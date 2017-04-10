/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;


import com.elster.jupiter.util.units.Quantity;

import java.util.List;

public class EndDeviceCommandInfo {
    public EndDeviceCommand command;
    public long effectiveTimestamp;
    public UsagePointCommandCallbackInfo httpCallBack;
}
