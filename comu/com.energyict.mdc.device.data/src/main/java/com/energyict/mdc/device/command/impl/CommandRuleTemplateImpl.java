package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRuleTemplate;
import com.energyict.mdc.device.command.impl.constraintvalidators.HasValidLimits;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@HasValidLimits(groups = {Save.Create.class, Save.Update.class})
public class CommandRuleTemplateImpl implements CommandRuleTemplate {

    enum Fields {

        NAME("name"),
        DAYLIMIT("dayLimit"),
        WEEKLIMIT("weekLimit"),
        MONTHLIMIT("monthLimit"),
        COMMANDS("commands");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }
    private long id;

    @Size(max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String name;
    private long dayLimit;
    private long weekLimit;
    private long monthLimit;
    private List<CommandInRule> commands = new ArrayList<>();

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

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
    public long getVersion() {
        return version;
    }

    @Override
    public void save() {

    }

    @Override
    public void delete() {

    }
}
