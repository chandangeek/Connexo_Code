/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FirmwareStatusInfo {
    @XmlJavaTypeAdapter(FirmwareStatusFieldAdapter.class)
    public FirmwareStatus id;
    public String localizedValue;

    public FirmwareStatusInfo() {
    }

    public FirmwareStatusInfo(FirmwareStatus firmwareStatus, Thesaurus thesaurus) {
        this.id = firmwareStatus;
        this.localizedValue = FirmwareStatusTranslationKeys.translationFor(id, thesaurus);
    }
}
