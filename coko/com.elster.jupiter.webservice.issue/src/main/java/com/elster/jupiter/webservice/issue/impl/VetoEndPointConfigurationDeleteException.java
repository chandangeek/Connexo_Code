/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public class VetoEndPointConfigurationDeleteException extends LocalizedException {
    public VetoEndPointConfigurationDeleteException(Thesaurus thesaurus, EndPointConfiguration endPointConfiguration) {
        super(thesaurus, MessageSeeds.END_POINT_CONFIG_IN_USE, endPointConfiguration.getName());
    }
}
