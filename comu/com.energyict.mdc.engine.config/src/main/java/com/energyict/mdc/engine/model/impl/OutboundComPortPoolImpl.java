package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}")
public class OutboundComPortPoolImpl extends ComPortPoolImpl implements OutboundComPortPool {

    public static final String FIELD_TASKEXECUTIONTOMEOUT = "taskExecutionTimeout";

    private final Provider<ComPortPoolMember> comPortPoolMemberProvider;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration taskExecutionTimeout;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    @Inject
    protected OutboundComPortPoolImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider, Thesaurus thesaurus, EventService eventService) {
        super(dataModel, thesaurus, eventService);
        this.comPortPoolMemberProvider = comPortPoolMemberProvider;
    }

    @Override
    public boolean isInbound () {
        return false;
    }

    @Override
    public TimeDuration getTaskExecutionTimeout() {
        return new TimeDuration(this.taskExecutionTimeout.getCount(), this.taskExecutionTimeout.getTimeUnitCode());
    }

    @Override
    public List<OutboundComPort> getComPorts() {
        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            outboundComPorts.add((OutboundComPort) comPortPoolMember.getComPort());
        }
        return ImmutableList.copyOf(outboundComPorts);
    }

    @Override
    public void addOutboundComPort(OutboundComPort outboundComPort) {
        validateComPortForComPortType(outboundComPort, this.getComPortType());
        ComPortPoolMember comPortPoolMember = comPortPoolMemberProvider.get();
        comPortPoolMember.setComPort(outboundComPort);
        comPortPoolMember.setComPortPool(this);

        this.comPortPoolMembers.add(comPortPoolMember);
    }

    @Override
    public void removeOutboundComPort(OutboundComPort outboundComPort) {
        Iterator<ComPortPoolMember> iterator = comPortPoolMembers.iterator();
        while(iterator.hasNext()) {
            ComPortPoolMember comPortPoolMember = iterator.next();
            if (comPortPoolMember.getComPort().getId()==outboundComPort.getId()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void setTaskExecutionTimeout(TimeDuration taskExecutionTimeout) {
        this.taskExecutionTimeout = new TimeDuration(taskExecutionTimeout.getCount(), taskExecutionTimeout.getTimeUnitCode());
    }

    @Override
    protected void makeMembersObsolete () {
        this.comPortPoolMembers.clear();
    }

}