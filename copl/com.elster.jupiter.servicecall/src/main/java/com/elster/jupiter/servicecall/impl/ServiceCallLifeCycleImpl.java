package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;

import javax.inject.Inject;

/**
 * Created by bvn on 2/4/16.
 */
public class ServiceCallLifeCycleImpl implements ServiceCallLifeCycle {

    public enum Fields {
        name("name"),
        finiteStateMachine("finiteStateMachine");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }

    }

    private long id;
    private String name;
    private final DataModel dataModel;
    private Reference<FiniteStateMachine> finiteStateMachine = Reference.empty();

    @Inject
    public ServiceCallLifeCycleImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void init(String name, FiniteStateMachine finiteStateMachine) {
        this.name = name;
        this.finiteStateMachine.set(finiteStateMachine);
    }

    public void save () {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        }
        else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    public FiniteStateMachine getFiniteStateMachine() {
        return finiteStateMachine.orElseThrow(IllegalStateException::new);
    }
}
