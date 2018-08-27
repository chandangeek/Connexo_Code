/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public class VetoDeleteEndPointConfigurationException extends LocalizedException {
    public VetoDeleteEndPointConfigurationException(Thesaurus thesaurus, EndPointConfiguration endPointConfiguration) {
        super(thesaurus, MessageSeeds.ENDPOINT_IS_USED_BY_EXPORT_TASK, endPointConfiguration.getName());
    }
}
