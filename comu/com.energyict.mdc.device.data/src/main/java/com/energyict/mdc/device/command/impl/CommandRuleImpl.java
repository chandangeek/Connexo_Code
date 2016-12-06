package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleTemplate;
import com.energyict.mdc.device.command.impl.constraintvalidators.HasValidLimits;
import com.energyict.mdc.device.command.impl.constraintvalidators.UniqueName;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.google.inject.Inject;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_NAME + "}")
@HasValidLimits(groups = {Save.Create.class, Save.Update.class})
public class CommandRuleImpl implements CommandRule {

    public enum Fields {

        NAME("name"),
        DAYLIMIT("dayLimit"),
        WEEKLIMIT("weekLimit"),
        MONTHLIMIT("monthLimit"),
        COMMANDS("commands"),
        COMMANDRULETEMPLATE("commandRuleTemplate");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }
    private long id;

    private final DataModel dataModel;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Size(max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String name;
    private long dayLimit;
    private long weekLimit;
    private long monthLimit;
    private Reference<CommandRuleTemplate> commandRuleTemplate;
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.AT_LEAST_ONE_COMMAND_REQUIRED + "}")
    @Valid
    private List<CommandInRule> commands = new ArrayList<>();

    @SuppressWarnings("unused")
    private String userName;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public CommandRuleImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.dataModel = dataModel;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDayLimit() {
        return dayLimit;
    }

    @Override
    public long getWeekLimit() {
        return weekLimit;
    }

    @Override
    public long getMonthLimit() {
        return monthLimit;
    }

    @Override
    public List<CommandInRule> getCommands() {
        return commands;
    }

    @Override
    public Optional<CommandRuleTemplate> getCommandRuleTemplate() {
        return commandRuleTemplate.getOptional();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void save() {
        if (this.getId() > 0) {
            doUpdate();
        } else {
            doSave();
        }
    }

    private void doSave() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDayLimit(long dayLimit) {
        this.dayLimit = dayLimit;
    }

    public void setWeekLimit(long weekLimit) {
        this.weekLimit = weekLimit;
    }

    public void setMonthLimit(long monthLimit) {
        this.monthLimit = monthLimit;
    }

    public void addCommand(DeviceMessageSpec deviceMessageSpec) {
        CommandInRuleImpl commandInRule = this.dataModel.getInstance(CommandInRuleImpl.class).initialize(deviceMessageSpec, this);
        this.commands.add(commandInRule);
    }
}
