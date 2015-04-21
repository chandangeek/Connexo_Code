package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.ComTaskBuilder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ComTaskTpl implements Template<ComTask, ComTaskBuilder> {
    READ_ALL("Read all",
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.GENERIC),
            Arrays.asList(RegisterGroupTpl.DEVICE_DATA),
            null,
            Arrays.asList(new ComTaskBuilder.Clock(ClockTaskType.SETCLOCK, TimeDuration.minutes(5), TimeDuration.hours(1), null))),
    TOPOLOGY("Topology",
            null,
            null,
            null,
            Arrays.asList(TopologyAction.VERIFY),
            null),
    TOPOLOGY_UPDATE("Topology update",
        null,
        null,
        null,
        Arrays.asList(TopologyAction.UPDATE),
        null),
    READ_REGISTER_DATA("Read register data",
            null,
            null,
            Arrays.asList(RegisterGroupTpl.DEVICE_DATA),
            null,
            null),
    READ_LOAD_PROFILE_DATA("Read load profile data",
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            null,
            null,
            null,
            null),
    READ_LOG_BOOK_DATA("Read logbook data",
            null,
            Arrays.asList(LogBookTypeTpl.GENERIC),
            null,
            null,
            null),
    ;
    private String name;
    private List<LoadProfileTypeTpl> loadProfileTypes;
    private List<LogBookTypeTpl> logBookTypes;
    private List<RegisterGroupTpl> registerGroups;
    private List<TopologyAction> topologyActions;
    private List<ComTaskBuilder.Clock> clocks;

    ComTaskTpl(String name, List<LoadProfileTypeTpl> loadProfileTypes, List<LogBookTypeTpl> logBookTypes, List<RegisterGroupTpl> registerGroups, List<TopologyAction> topologyActions, List<ComTaskBuilder.Clock> clocks) {
        this.name = name;
        this.loadProfileTypes = loadProfileTypes;
        this.logBookTypes = logBookTypes;
        this.registerGroups = registerGroups;
        this.topologyActions = topologyActions;
        this.clocks = clocks;
    }

    @Override
    public Class<ComTaskBuilder> getBuilderClass() {
        return ComTaskBuilder.class;
    }

    @Override
    public ComTaskBuilder get(ComTaskBuilder builder) {
        builder.withName(this.name);
        if (loadProfileTypes != null){
            builder.withLoadProfileTypes(loadProfileTypes.stream().map(lptDescr -> Builders.from(lptDescr).get()).collect(Collectors.toList()));
        }
        if (logBookTypes != null){
            builder.withLogBookTypes(logBookTypes.stream().map(lbtDescr -> Builders.from(lbtDescr).get()).collect(Collectors.toList()));
        }
        if (registerGroups != null){
            builder.withRegisterGroups(registerGroups.stream().map(rgDescr -> Builders.from(rgDescr).get()).collect(Collectors.toList()));
        }
        builder.withTopologyActions(topologyActions);
        builder.withClocks(clocks);
        return builder;
    }
}
