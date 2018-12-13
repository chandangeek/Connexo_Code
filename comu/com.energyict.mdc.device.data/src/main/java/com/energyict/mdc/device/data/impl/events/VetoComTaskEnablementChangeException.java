/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.impl.MessageSeeds;

public class VetoComTaskEnablementChangeException extends LocalizedException {

    public VetoComTaskEnablementChangeException(Thesaurus thesaurus, ComTaskEnablement comTaskEnablement) {
        super(thesaurus, MessageSeeds.COM_TASK_ENABLEMENT_UPDATE_RESTRICTED);
        this.set("comTaskEnablementId", comTaskEnablement.getId());
    }

}