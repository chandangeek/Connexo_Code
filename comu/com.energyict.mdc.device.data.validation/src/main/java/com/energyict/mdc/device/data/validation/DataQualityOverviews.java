/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface DataQualityOverviews {

    List<DataQualityOverview> allOverviews();

}