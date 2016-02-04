package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecalls.ServiceCallLifeCycle;

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
    private Reference<FiniteStateMachine> finiteStateMachine;

}
