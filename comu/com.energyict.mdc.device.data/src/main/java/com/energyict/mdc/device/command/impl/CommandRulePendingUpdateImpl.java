package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.impl.constraintvalidators.HasValidLimits;
import com.elster.jupiter.dualcontrol.PendingUpdate;
import com.elster.jupiter.orm.DataModel;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@HasValidLimits(groups = {Save.Create.class, Save.Update.class})
public class CommandRulePendingUpdateImpl implements CommandRulePendingUpdate {

    enum Fields {

        NAME("name"),
        ACTIVE("active"),
        DAYLIMIT("dayLimit"),
        WEEKLIMIT("weekLimit"),
        MONTHLIMIT("monthLimit"),
        COMMANDS("commands"),
        ISACTIVATION("isActivation"),
        ISDEACTIVATION("isDeactivation"),
        ISREMOVAL("isRemoval");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }

    private final DataModel dataModel;

    private long id;

    @Size(max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String name;
    private long dayLimit;
    private long weekLimit;
    private long monthLimit;
    private boolean active = false;
    private boolean isActivation = false;
    private boolean isDeactivation = false;
    private boolean isRemoval = false;
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
    public CommandRulePendingUpdateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    CommandRulePendingUpdateImpl initialize(CommandRule commandRule) {
        this.name = commandRule.getName();
        this.dayLimit = commandRule.getDayLimit();
        this.weekLimit = commandRule.getWeekLimit();
        this.monthLimit = commandRule.getMonthLimit();
        this.active = commandRule.isActive();
        return this;
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
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.isActivation = active;
        this.isDeactivation = !active;
        this.isRemoval = false;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public boolean isActivation() {
        return isActivation;
    }

    @Override
    public boolean isDeactivation() {
        return isDeactivation;
    }

    @Override
    public boolean isRemoval() {
        return isRemoval;
    }
}
