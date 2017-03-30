/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class DetailedValidationInfo {

    public Boolean validationActive;
    public Boolean channelValidationStatus;
    public Boolean dataValidated;
    public Set<ValidationRuleInfoWithNumber> suspectReason;
    public Long lastChecked;

    public DetailedValidationInfo() {

    }

}
