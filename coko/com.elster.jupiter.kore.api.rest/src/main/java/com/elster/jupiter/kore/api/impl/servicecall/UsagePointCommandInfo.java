package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.util.units.Quantity;

/**
 * Created by antfom on 08.06.2016.
 */
public class UsagePointCommandInfo {
    public String command;
    public long effectiveTimestamp;
    public Quantity loadLimit;
    public UsagePointCommandCallbackInfo httpCallBack;
}
