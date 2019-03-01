/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.ComScheduleBuilder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ComScheduleTpl implements Template<ComSchedule, ComScheduleBuilder> {
    DAILY_READ_ALL("Daily read all", Arrays.asList(ComTaskTpl.BASIC_CHECK, ComTaskTpl.READ_REGISTER_DATA, ComTaskTpl.READ_LOAD_PROFILE_DATA, ComTaskTpl.READ_LOG_BOOK_DATA, ComTaskTpl.VERIFY_STATUS_INFO, ComTaskTpl.READ_ALL, ComTaskTpl.TOPOLOGY_VERIFY, ComTaskTpl.BEACON_INBOUND), TimeDuration
            .days(1)),
    DAILY_READ_ALL_GAS("Daily read all gas", Arrays.asList(ComTaskTpl.BASIC_CHECK, ComTaskTpl.READ_REGISTER_DATA_GAS, ComTaskTpl.READ_LOAD_PROFILE_DATA_GAS, ComTaskTpl.VERIFY_STATUS_INFO), TimeDuration.days(1)),
    DAILY_READ_ALL_WATER("Daily read all water", Arrays.asList(ComTaskTpl.BASIC_CHECK, ComTaskTpl.READ_REGISTER_DATA_WATER, ComTaskTpl.READ_LOAD_PROFILE_DATA_WATER, ComTaskTpl.VERIFY_STATUS_INFO), TimeDuration.days(1)),
    ;

    private String name;
    private List<ComTaskTpl> comTasks;
    private TimeDuration every;

    ComScheduleTpl(String name, List<ComTaskTpl> comTasks, TimeDuration every) {
        this.name = name;
        this.comTasks = comTasks;
        this.every = every;
    }

    @Override
    public Class<ComScheduleBuilder> getBuilderClass() {
        return ComScheduleBuilder.class;
    }

    @Override
    public ComScheduleBuilder get(ComScheduleBuilder builder) {
        return builder.withName(name).withTimeDuration(every).withComTasks(comTasks.stream().map(ctDescr -> Builders.from(ctDescr).get()).collect(Collectors.toList()));
    }
}
