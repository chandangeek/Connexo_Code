/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.alarms.entity.CreationRuleActionPhase;
import com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys;


public class CreationRuleActionPhaseInfo {
    public String uuid;
    public String title;
    public String description;

    public CreationRuleActionPhaseInfo() {}

    public CreationRuleActionPhaseInfo(CreationRuleActionPhase phase, Thesaurus thesaurus) {
        this();
        this.uuid = phase.name();
        this.title = thesaurus.getFormat(DeviceAlarmTranslationKeys.from(phase.getTitleId())).format();
        this.description = thesaurus.getFormat(DeviceAlarmTranslationKeys.from(phase.getDescriptionId())).format();
    }

}