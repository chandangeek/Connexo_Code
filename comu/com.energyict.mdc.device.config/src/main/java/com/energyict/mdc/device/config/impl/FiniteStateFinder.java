package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;

import java.util.Optional;

public class FiniteStateFinder implements CanFindByLongPrimaryKey<State> {

    private final FiniteStateMachineService finiteStateMachineService;

    public FiniteStateFinder(FiniteStateMachineService finiteStateMachineService) {
        super();
        this.finiteStateMachineService = finiteStateMachineService;
    }


    @Override
    public FactoryIds factoryId() {
        return FactoryIds.FINITE_STATE;
    }

    @Override
    public Class<State> valueDomain() {
        return State.class;
    }

    @Override
    public Optional<State> findByPrimaryKey(long id) {
        return this.finiteStateMachineService.findFiniteStateById(id);
    }

}