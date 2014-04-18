package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.ArrayList;
import java.util.List;

public class ComScheduleImpl implements ComSchedule {

    enum Fields {
        NAME("name"),
        MOD_DATE("mod_date"),
        COM_TASK_IN_COM_SCHEDULE("comTaskUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }
    private long id;
    private String name;
    private List<ComTaskInComSchedule> comTaskUsages = new ArrayList<>();
//    private Reference<NextExecutionSpecs> nextExecutionSpecsReference = ValueReference.absent();


    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
