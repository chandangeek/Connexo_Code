package com.energyict.mdc.device.command;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

public interface ServerCommandRule extends HasId, HasName {
    long getDayLimit();
    long getWeekLimit();
    long getMonthLimit();
}
