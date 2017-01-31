/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

interface IEstimationService extends EstimationService {

    Thesaurus getThesaurus();

    EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, Logger logger);

    EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, ReadingType readingType, Logger logger);

    DestinationSpec getDestination();

    Optional<? extends EstimationTask> findEstimationTask(RecurrentTask recurrentTask);

    EstimationReport estimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, Logger logger);

    List<EstimationTask> findByDeviceGroup(EndDeviceGroup endDeviceGroup, int skip, int limit);

    List<EstimationResolver> getEstimationResolvers();
}
