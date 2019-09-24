/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.ComPortPoolMember;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.ports.ComPortType;

import com.google.inject.Provider;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link OutboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}")
public final class OutboundComPortPoolImpl extends ComPortPoolImpl implements OutboundComPortPool {

    public static final String FIELD_TASKEXECUTIONTOMEOUT = "taskExecutionTimeout";

    private Provider<ComPortPoolMember> comPortPoolMemberProvider;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration taskExecutionTimeout;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    private boolean inbound;

    protected OutboundComPortPoolImpl() {
        super();
    }

    @Inject
    protected OutboundComPortPoolImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider, Thesaurus thesaurus, EventService eventService) {
        super(dataModel, thesaurus, eventService);
        this.comPortPoolMemberProvider = comPortPoolMemberProvider;
    }

    OutboundComPortPoolImpl initialize(String name, ComPortType comPortType, TimeDuration taskExecutionTimeout, long pctHighPrioTasks) {
        this.setName(name);
        this.setComPortType(comPortType);
        this.setTaskExecutionTimeout(taskExecutionTimeout);
        this.setPctHighPrioTasks(pctHighPrioTasks);
        return this;
    }

    @Override
    @XmlAttribute
    public boolean isInbound () {
        inbound = false;
        return inbound;
    }

    @Override
    @XmlAttribute
    public TimeDuration getTaskExecutionTimeout() {
        return new TimeDuration(this.taskExecutionTimeout.getCount(), this.taskExecutionTimeout.getTimeUnitCode());
    }

    @Override
    @XmlElement(type = OutboundComPortImpl.class, name = "comPorts")
    public List<OutboundComPort> getComPorts() {
        return comPortPoolMembers.stream().map(ComPortPoolMember::getComPort).collect(Collectors.toList());
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

    public void setTaskExecutionTimeout(TimeDuration taskExecutionTimeout) {
        this.taskExecutionTimeout = new TimeDuration(taskExecutionTimeout.getCount(), taskExecutionTimeout.getTimeUnitCode());
    }

    @Override
    protected void makeMembersObsolete () {
        this.comPortPoolMembers.clear();
    }

}