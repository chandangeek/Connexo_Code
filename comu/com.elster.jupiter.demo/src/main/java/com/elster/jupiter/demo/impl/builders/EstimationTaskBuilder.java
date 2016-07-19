package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Copyrights EnergyICT
 * Date: 1/10/2015
 * Time: 14:54
 */
public class EstimationTaskBuilder extends NamedBuilder<EstimationTask, EstimationTaskBuilder> {

    private final EstimationService estimationService;
    private EndDeviceGroup deviceGroup;
    private ScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(8, 0, 0).build();

    public EstimationTaskBuilder(EstimationService estimationService){
        super(EstimationTaskBuilder.class);
        this.estimationService = estimationService;
    }

    public EstimationTaskBuilder withEndDeviceGroup(EndDeviceGroup group){
        this.deviceGroup = group;
        return this;
    }

    public EstimationTaskBuilder withScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public Optional<EstimationTask> find() {
        return estimationService.getEstimationTaskQuery()
                .select(where("name").isEqualTo(getName()).and(where("obsoleteTime").isNull()))
                .stream().map(EstimationTask.class::cast)
                .findFirst();
    }

    @Override
    public EstimationTask create() {
        Log.write(this);
        com.elster.jupiter.estimation.EstimationTaskBuilder taskBuilder = estimationService.newBuilder();
        taskBuilder.setName(getName());
        taskBuilder.setQualityCodeSystem(QualityCodeSystem.MDC);
        taskBuilder.setEndDeviceGroup(deviceGroup);
        taskBuilder.setScheduleExpression(scheduleExpression);

        EstimationTask task = taskBuilder.create();
        applyPostBuilders(task);

        return task;
    }
}
