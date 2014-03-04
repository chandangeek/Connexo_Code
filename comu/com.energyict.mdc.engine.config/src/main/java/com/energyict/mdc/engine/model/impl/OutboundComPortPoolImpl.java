package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Create.class }, message = "{MDC.ComPortTypeOfComPortDoesNotMatchWithComPortPool}")
public class OutboundComPortPoolImpl extends ComPortPoolImpl implements OutboundComPortPool {

    public static final String FIELD_TASKEXECUTIONTOMEOUT = "taskExecutionTimeout";

    private final Provider<ComPortPoolMember> comPortPoolMemberProvider;
    @NotNull(groups = { Save.Update.class, Save.Create.class }, message = "{MDC.CanNotBeEmpty}")
    private TimeDuration taskExecutionTimeout;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    @Inject
    protected OutboundComPortPoolImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
        super(dataModel);
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
    protected void validateDelete() {
        this.validateNotUsedByConnectionMethods();
    }

    @Override
    protected void validateMakeObsolete() {
        super.validateMakeObsolete();
        this.validateNotUsedByConnectionMethods();
    }

    private void validateNotUsedByConnectionMethods() {
        // TODO replace with Event
//        List<ServerConnectionMethod> connectionMethods = this.getConnectionMethodFactory().findByPool(this);
//        if (!connectionMethods.isEmpty()) {
//            List<ConnectionTask> connectionTasks = this.collectConnectionTasks(connectionMethods);
//            String names = CollectionFormatter.toSeparatedList(toNames(connectionTasks), ",");
//            throw new BusinessException(
//                    "outboundComPortPoolXStillInUseByConnectionMethodY",
//                    "Outbound ComPortPool '{0}' is still in use by the following connection tasks: {1}",
//                    this.getName(),
//                    names);
//        }
    }

    @Override
    protected void makeMembersObsolete () {
        this.comPortPoolMembers.clear();
    }


}