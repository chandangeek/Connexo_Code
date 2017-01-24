package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.ComTaskBuilder;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Template} holding a set of predefined attributes for creating Communication Tasks
 */
public enum ComTaskTpl implements Template<ComTask, ComTaskBuilder> {
    READ_ALL("Read all",
            Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Arrays.asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            Collections.singletonList(RegisterGroupTpl.DEVICE_DATA),
            null,
            Collections.singletonList(new ComTaskBuilder.Clock(ClockTaskType.SETCLOCK, TimeDuration.minutes(5), TimeDuration.hours(1), null))),
    READ_REGISTER_DATA("Read register data",
            null,
            null,
            Collections.singletonList(RegisterGroupTpl.DEVICE_DATA),
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
            Arrays.asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG),
            null,
            null,
            null),
    TOPOLOGY("Topology",
            null,
            null,
            null,
            Collections.singletonList(TopologyAction.VERIFY),
            null),
    TOPOLOGY_UPDATE("Topology update",
            null,
            null,
            null,
            Collections.singletonList(TopologyAction.UPDATE),
            null),
    VERIFY_STATUS_INFO("Verify status information",
            null,
            null,
            null,
            null,
            null) {
        @Override
        protected boolean isVerifyStatusInformationTask() {
            return true;
        }
    },
    //System communication task, it should be available out of the box
    // We don't need to create it, just link to device configurations
    FIRMWARE_MANAGEMENT("Firmware management",
            null,
            null,
            null,
            null,
            null) {
    },
    READ_DATA_LOGGER_REGISTER_DATA("Read data logger register data",
            null,
            null,
            Collections.singletonList(RegisterGroupTpl.DATA_LOGGER_REGISTER_DATA),
            null,
            null),
    READ_DATA_LOGGER_LOAD_PROFILE_DATA("Read data logger load profile data",
            Collections.singletonList(LoadProfileTypeTpl.DATA_LOGGER_32),
            null,
            null,
            null,
            null),
    COMMANDS("Commands",
            null,
            null,
            null,
            null,
            null) {
        @Override
        protected Function<DeviceMessageSpecificationService, List<DeviceMessageCategory>> getCommandCategoryProvider() {
            return DeviceMessageSpecificationService::filteredCategoriesForComTaskDefinition;
        }
    },
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
        if (loadProfileTypes != null) {
            builder.withLoadProfileTypes(loadProfileTypes.stream().map(lptDescr -> Builders.from(lptDescr).get()).collect(Collectors.toList()));
        }
        if (logBookTypes != null) {
            builder.withLogBookTypes(logBookTypes.stream().map(lbtDescr -> Builders.from(lbtDescr).get()).collect(Collectors.toList()));
        }
        if (registerGroups != null) {
            builder.withRegisterGroups(registerGroups.stream().map(rgDescr -> Builders.from(rgDescr).get()).collect(Collectors.toList()));
        }
        builder.withCommandCategoryProvider(getCommandCategoryProvider());
        builder.withTopologyActions(topologyActions);
        builder.withClocks(clocks);
        builder.forStatusInformationTask(isVerifyStatusInformationTask());
        return builder;
    }

    public String getName() {
        return name;
    }

    protected boolean isVerifyStatusInformationTask() {
        return false;
    }

    protected Function<DeviceMessageSpecificationService, List<DeviceMessageCategory>> getCommandCategoryProvider() {
        return null;
    }

    static ComTaskTpl[] excludeTopologyTpls() {
        return EnumSet.of(READ_REGISTER_DATA, READ_LOAD_PROFILE_DATA, READ_LOG_BOOK_DATA, VERIFY_STATUS_INFO, FIRMWARE_MANAGEMENT, COMMANDS).toArray(new ComTaskTpl[6]);
    }
}
