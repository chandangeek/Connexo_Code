package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.UnderDualControl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.impl.constraintvalidators.HasUniqueCommands;
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
import java.util.Objects;
import java.util.Optional;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_NAME + "}")
@HasValidLimits(groups = {Save.Create.class, Save.Update.class})
@HasUniqueCommands(groups= {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_COMMAND + "}")
public class CommandRuleImpl implements CommandRule, UnderDualControl<CommandRulePendingUpdate> {

    public enum Fields {

        NAME("name"),
        ACTIVE("active"),
        DAYLIMIT("dayLimit"),
        WEEKLIMIT("weekLimit"),
        MONTHLIMIT("monthLimit"),
        COMMANDS("commands"),
        COMMANDRULETEMPLATE("commandRulePendingUpdate"),
        MONITOR("monitor");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    private final DataModel dataModel;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private DualControlService dualControlService;

    private long id;
    @Size(max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String name;
    private long dayLimit;
    private long weekLimit;
    private long monthLimit;
    private boolean active = false;

    private Reference<Monitor> monitor = Reference.empty();
    private Reference<CommandRulePendingUpdate> commandRulePendingUpdate = Reference.empty();
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
    public CommandRuleImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService, DualControlService dualControlService) {
        this.dataModel = dataModel;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.dualControlService = dualControlService;
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

    public Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate() {
        return commandRulePendingUpdate.getOptional();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        if(this.isActive()) {
            throw new IllegalArgumentException("Already active");
        }
        CommandRulePendingUpdateImpl update = new CommandRulePendingUpdateImpl(dataModel);
        update.initialize(this);
        update.setActive(true);
        update.save();
        getMonitor().request(update, this);
    }

    @Override
    public void deactivate() {
        if(!this.isActive()) {
            throw new IllegalArgumentException("Already inactive");
        }
        CommandRulePendingUpdateImpl update = new CommandRulePendingUpdateImpl(dataModel);
        update.initialize(this);
        update.setActive(false);
        update.save();
        getMonitor().request(update, this);
    }

    @Override
    public void approve() {
        this.getMonitor().approve(this);
    }

    @Override
    public void reject() {
        this.getMonitor().reject(this);
    }

    @Override
    public boolean hasCurrentUserAccepted() {
        return this.commandRulePendingUpdate.isPresent() && this.getMonitor().hasCurrentUserAccepted();
    }

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

    public void delete() {
        if(!active) {
            dataModel.remove(this);
        } else {
            createDeleteUpdate();
        }
    }

    private void createDeleteUpdate() {
        CommandRulePendingUpdateImpl update = new CommandRulePendingUpdateImpl(dataModel);
        update.initializeRemoval(this);
        update.save();
        getMonitor().request(update, this);
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

    @Override
    public Monitor getMonitor() {
        if (!monitor.isPresent()) {
            monitor.set(dualControlService.createMonitor());
        }
        return monitor.get();
    }

    @Override
    public Optional<CommandRulePendingUpdate> getPendingUpdate() {
       return commandRulePendingUpdate.getOptional();
    }

    @Override
    public void setPendingUpdate(CommandRulePendingUpdate commandRuleTemplate) {
        this.commandRulePendingUpdate.set(commandRuleTemplate);
        this.save();
    }

    @Override
    public void applyUpdate() {
        getPendingUpdate().ifPresent(commandRulePendingUpdate -> {
            if (commandRulePendingUpdate.isRemoval()) {
                this.delete();
                return;
            }
            name = commandRulePendingUpdate.getName();
            dayLimit = commandRulePendingUpdate.getDayLimit();
            weekLimit = commandRulePendingUpdate.getWeekLimit();
            monthLimit = commandRulePendingUpdate.getMonthLimit();
            active = commandRulePendingUpdate.isActive();
        });
        this.save();
    }

    @Override
    public void clearUpdate() {
        if(commandRulePendingUpdate.isPresent()) {
            CommandRulePendingUpdate entity = commandRulePendingUpdate.get();
            commandRulePendingUpdate.setNull();
            this.save();
            dataModel.remove(entity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandRuleImpl that = (CommandRuleImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
