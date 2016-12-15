package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import com.google.inject.Inject;

public class CommandRuleStats {

    private final DataModel dataModel;

    public enum Fields {

        ID("ID"),
        NR_OF_COMMAND_RULES("nrOfMessageRules"),
        NR_OF_COUNTERS("nrOfCounters");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    public static long ID = 1;
    private long nrOfMessageRules;
    private long nrOfCounters;

    @Inject
    public CommandRuleStats(DataModel dataModel) {
        this.dataModel = dataModel;
    }


    public long getNrOfMessageRules() {
        return nrOfMessageRules;
    }

    public long getNrOfCounters() {
        return nrOfCounters;
    }

    public void increaseNumberOfCommandRules() {
        nrOfMessageRules++;
        save();
    }

    public void decreaseNumberOfCommandRules() {
        nrOfMessageRules--;
        save();
    }

    public void increaseNumberOfCommandRuleCounters() {
        nrOfCounters++;
        save();
    }

    public void decreaseNumberOfCommandRuleCounters() {
        nrOfCounters--;
        save();
    }


    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
