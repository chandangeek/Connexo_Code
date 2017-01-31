/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl.actions;


import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.TranslationKeys;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RetryEstimationAction extends AbstractIssueAction {

    private static final String NAME = "RetryEstimationAction";

    private IssueService issueService;
    private EstimationService estimationService;

    @Inject
    public RetryEstimationAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, EstimationService estimationService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.estimationService = estimationService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.update();
            if (retry(issue)) {
                result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION_SUCCESS).format());
            } else {
                result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION_FAIL).format());
            }
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof IssueDataValidation){
            IssueDataValidation dvIssue = (IssueDataValidation) issue;
            if (!dvIssue.getStatus().isHistorical()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION).format();
    }

    private boolean retry(Issue issue) {
        IssueDataValidation dviIssue = (IssueDataValidation) issue;

        List<EstimationBlock> nonEstimatedBlocks = new ArrayList<>();
        dviIssue.getNotEstimatedBlocks().stream().forEach(block -> {
            Channel channel = block.getChannel();
            Optional<Meter> meter = channel.getChannelsContainer().getMeter();
            if (meter.isPresent()) {
                Optional<? extends MeterActivation> meterActivation = meter.get().getCurrentMeterActivation();
                if (meterActivation.isPresent()) {
                    EstimationReport estimationReport = estimationService.previewEstimate(QualityCodeSystem.MDC,
                            channel.getChannelsContainer(),
                            Range.openClosed(block.getStartTime(), block.getEndTime()));

                    List<BaseReading> editedBulkReadings = new ArrayList<>();
                    estimationReport.getResults().entrySet().forEach(result -> {
                        nonEstimatedBlocks.addAll(result.getValue().remainingToBeEstimated());
                        result.getValue().estimated()
                                .forEach(estimated -> estimated.estimatables()
                                        .forEach(value -> {
                                            editedBulkReadings.add(IntervalReadingImpl.of(value.getTimestamp(), value.getEstimation()));
                                        }));
                    });
                    channel.editReadings(QualityCodeSystem.MDC, editedBulkReadings);
                }
            }
        });

        return (nonEstimatedBlocks.size() == 0);
    }
}
