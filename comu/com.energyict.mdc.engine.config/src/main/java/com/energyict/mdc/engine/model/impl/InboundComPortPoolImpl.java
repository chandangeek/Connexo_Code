package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.InboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
public class InboundComPortPoolImpl extends ComPortPoolImpl implements InboundComPortPool {

    private long discoveryProtocolPluggableClassId;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    @Inject
    protected InboundComPortPoolImpl(DataModel dataModel, EngineModelService engineModelService) {
        super(dataModel, engineModelService);
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public List<InboundComPort> getComPorts() {
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            inboundComPorts.add((InboundComPort) comPortPoolMember.getComPort());
        }
        return ImmutableList.copyOf(inboundComPorts);
    }

    @Override
    public long getDiscoveryProtocolPluggableClassId() {
        return this.discoveryProtocolPluggableClassId;
    }

    @Override
    public void setDiscoveryProtocolPluggableClassId(long discoveryProtocolPluggableClassId) {
        this.discoveryProtocolPluggableClassId = discoveryProtocolPluggableClassId;
    }

    protected void validate() {
        super.validate();
        this.validateComPorts(this.getComPorts(), this.getComPortType());
        this.validateDiscoveryProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
    }

    private void validateDiscoveryProtocolPluggableClass(long discoveryProtocolPluggableClassId) {
        if (discoveryProtocolPluggableClassId == 0) {
            throw new TranslatableApplicationException("XcannotBeEmpty", "\"{0}\" is a required property", "inboundComPortPool.discoveryProtocol");
            // TODO Use PluggableClassService once JP-682 is done
//        } else {
//            PluggableClass pluggableClass = this.findDiscoveryProtocolPluggableClass(discoveryProtocolPluggableClassId);
//            if (pluggableClass == null) {
//                throw InvalidReferenceException.newForIdBusinessObject(discoveryProtocolPluggableClassId, this.getInboundDeviceProtocolPluggableClassFactory());
//            }
        }
    }

    /**
     * Validates that all referenced {@link com.energyict.mdc.engine.model.ComPort} are effectively {@link com.energyict.mdc.engine.model.OutboundComPort}
     * and that their {@link ComPortType type} corresponds with this pool's type.
     *
     * @param inboundComPorts The ids of the referenced ComPorts
     * @param comPortType       The ComPortType of this OutboundComPortPool
     */
    private void validateComPorts(List<InboundComPort> inboundComPorts, ComPortType comPortType) {
        for (InboundComPort comPort : inboundComPorts) {
            this.validateComPortForComPortType(comPort, comPortType);

        }
    }

    protected void validateDelete() {
        this.validateNotUsedByComPorts();
        this.validateNotUsedByConnectionTasks();
    }

    @Override
    protected void validateMakeObsolete() {
        super.validateMakeObsolete();
        this.validateNotUsedByComPorts();
        this.validateNotUsedByConnectionTasks();
    }

    private void validateNotUsedByComPorts() {

        List<InboundComPort> comPorts = this.getComPorts();
        if (!comPorts.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (InboundComPort comPort : comPorts) {
                names.add(comPort.getName());
            }

            throw new TranslatableApplicationException(
                    "inboundComPortPoolXStillInUseByComPortsY",
                    "Inbound ComPortPool '{0}' is still in use by the following inbound comport(s): {1}",
                    this.getName(),
                    Joiner.on(",").skipNulls().join(names));
        }
    }

    private void validateNotUsedByConnectionTasks () {
        // TODO replace by event
//        ServerConnectionTaskFactory connectionTaskFactory = this.getConnectionTaskFactory();
//        List<InboundConnectionTask> connectionTasks = connectionTaskFactory.findInboundUsingComPortPool(this);
//        if (!connectionTasks.isEmpty()) {
//            String names = CollectionFormatter.toSeparatedList(toNames(connectionTasks), ",");
//            throw new BusinessException(
//                    "inboundComPortPoolXStillInUseByConnectionTasksY",
//                    "Inbound ComPortPool '{0}' is still in use by the following inbound connection tasks(s): {1}",
//                    this.getName(),
//                    names);
//        }
    }

    @Override
    protected void makeMembersObsolete() {
        /* Can only be made obsolete if there are no members
         * so nothing to do here. */
    }


}