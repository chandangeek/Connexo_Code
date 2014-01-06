package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
public class OutboundComPortPoolImpl extends ComPortPoolImpl implements OutboundComPortPool {

    private TimeDuration taskExecutionTimeout;
    private final List<OutboundComPort> outboundComPorts = new ArrayList<>();

    public static OutboundComPortPool from(DataModel dataModel) {
        return dataModel.getInstance(OutboundComPortPoolImpl.class);
    }

    protected OutboundComPortPoolImpl () {
        super();
    }

    @Override
    public boolean isInbound () {
        return false;
    }

    @Override
    public TimeDuration getTaskExecutionTimeout () {
        return taskExecutionTimeout;
    }

    @Override
    public List<OutboundComPort> getComPorts () {
        return ImmutableList.copyOf(this.outboundComPorts);
    }

    @Override
    public void setComPorts(List<OutboundComPort> comPorts) {
        this.outboundComPorts.clear();
        this.outboundComPorts.addAll(comPorts);
    }

    @Override
    public void setTaskExecutionTimeout(TimeDuration taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

    protected void validate() {
        super.validate();
        this.validateNotNull(this.taskExecutionTimeout, "outboundComPortPool.taskExecutionTimeout");
        this.validateComPorts(this.outboundComPorts, this.getComPortType());
    }

    /**
     * Validates that all referenced {@link com.energyict.mdc.engine.model.ComPort} are effectively {@link com.energyict.mdc.engine.model.OutboundComPort}
     * and that their {@link ComPortType type} corresponds with this pool's type.
     *
     * @param outboundComPortIds The ids of the referenced ComPorts
     * @param comPortType The ComPortType of this OutboundComPortPool
     */
    private void validateComPorts(List<OutboundComPort> outboundComPortIds, ComPortType comPortType) {
        for (OutboundComPort comPortId : outboundComPortIds) {
            this.validateComPortForComPortType(comPortId, comPortType);

        }
    }

    @Override
    protected void validateDelete() {
        super.validateDelete();
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
        this.outboundComPorts.clear();
    }


}