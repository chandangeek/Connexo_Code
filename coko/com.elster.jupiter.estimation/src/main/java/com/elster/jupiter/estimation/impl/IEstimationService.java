package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public interface IEstimationService extends EstimationService {

    Thesaurus getThesaurus();

    EstimationReportImpl previewEstimate(MeterActivation meterActivation, Logger logger);

    EstimationReport estimate(MeterActivation meterActivation, ReadingType readingType, Logger logger);

    DestinationSpec getDestination();

    Optional<? extends EstimationTask> findEstimationTask(RecurrentTask recurrentTask);

    EstimationReport estimate(MeterActivation meterActivation, Logger logger);

    List<EstimationTask> findByDeviceGroup(EndDeviceGroup endDeviceGroup, int skip, int limit);
}
