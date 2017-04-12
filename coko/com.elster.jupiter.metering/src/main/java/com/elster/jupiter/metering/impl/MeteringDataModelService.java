/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.time.Clock;
import java.util.List;

public interface MeteringDataModelService {
    String COMPONENT_NAME = "MTR";

    DataModel getDataModel();

    Thesaurus getThesaurus();

    Clock getClock();

    void copyKeyIfMissing(NlsKey name, String localKey);

    List<HeadEndInterface> getHeadEndInterfaces();

    void addHeadEndInterface(HeadEndInterface headEndInterface);

    void removeHeadEndInterface(HeadEndInterface headEndInterface);

    void addCustomUsagePointMeterActivationValidator(com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator);

    void removeCustomUsagePointMeterActivationValidator(com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator);

    void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws CustomUsagePointMeterActivationValidationException;

    ServerMeteringService getMeteringService();

    MeteringTranslationService getMeteringTranslationService();

    DataAggregationService getDataAggregationService();

    ServerMetrologyConfigurationService getMetrologyConfigurationService();

}
