package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.Min;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.InboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Update.class }, message = "{MDC.ComPortTypeOfComPortDoesNotMatchWithComPortPool}")
public class InboundComPortPoolImpl extends ComPortPoolImpl implements InboundComPortPool {

    public static final String FIELD_DISCOVEYPROTOCOL = "discoveryProtocolPluggableClassId";

    private final EngineModelService engineModelService;
    @Min(value =1, groups = {Save.Create.class, Save.Update.class }, message = "{MDC.CanNotBeEmpty}")
    private long discoveryProtocolPluggableClassId;

    @Inject
    protected InboundComPortPoolImpl(DataModel dataModel, EngineModelService engineModelService) {
        super(dataModel);
        this.engineModelService = engineModelService;
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public List<InboundComPort> getComPorts() {
        return engineModelService.findInboundInPool(this);
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