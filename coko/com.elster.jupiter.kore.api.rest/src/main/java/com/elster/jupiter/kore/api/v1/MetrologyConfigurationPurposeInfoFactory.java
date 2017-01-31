/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

public class MetrologyConfigurationPurposeInfoFactory {

    public MetrologyConfigurationPurposeInfo asInfo(Long id, String name, Boolean required, String status) {
        MetrologyConfigurationPurposeInfo info = new MetrologyConfigurationPurposeInfo();
        info.id = id;
        info.name = name;
        info.required = required;
        info.status = status;
        return info;
    }
}
