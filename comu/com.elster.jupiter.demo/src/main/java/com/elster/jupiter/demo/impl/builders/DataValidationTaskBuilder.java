package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 1/10/2015
 * Time: 14:54
 */
public class DataValidationTaskBuilder extends NamedBuilder<DataValidationTask, DataValidationTaskBuilder> {


    private final ValidationService validationService;
    private EndDeviceGroup deviceGroup;
    private ScheduleExpression scheduleExpression = PeriodicalScheduleExpression.every(1).days().at(6, 0, 0).build();

    public DataValidationTaskBuilder(ValidationService validationService){
        super(DataValidationTaskBuilder.class);
        this.validationService = validationService;
    }

    public DataValidationTaskBuilder withEndDeviceGroup(EndDeviceGroup group){
        this.deviceGroup = group;
        return this;
    }

    public DataValidationTaskBuilder withScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public Optional<DataValidationTask> find() {
        return validationService.findValidationTaskByName(getName());
    }

    @Override
    public DataValidationTask create() {
        Log.write(this);
        com.elster.jupiter.validation.DataValidationTaskBuilder taskBuilder = validationService.newTaskBuilder();
        taskBuilder.setName(getName());
        taskBuilder.setQualityCodeSystem(QualityCodeSystem.MDC);
        taskBuilder.setEndDeviceGroup(deviceGroup);
        taskBuilder.setScheduleExpression(scheduleExpression);

        DataValidationTask task = taskBuilder.create();
        applyPostBuilders(task);

        return task;
    }
}
