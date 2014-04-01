package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
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
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}")
public class InboundComPortPoolImpl extends ComPortPoolImpl implements InboundComPortPool {

    public static final String FIELD_DISCOVEYPROTOCOL = "discoveryProtocolPluggableClassId";

    private final EngineModelService engineModelService;
    private final ProtocolPluggableService pluggableService;
    @Min(value =1, groups = {Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private long discoveryProtocolPluggableClassId;

    @Inject
    protected InboundComPortPoolImpl(DataModel dataModel, EngineModelService engineModelService, Thesaurus thesaurus, ProtocolPluggableService pluggableService) {
        super(dataModel, thesaurus);
        this.engineModelService = engineModelService;
        this.pluggableService = pluggableService;
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
    public DeviceProtocolPluggableClass getDiscoveryProtocolPluggableClass() {
        return pluggableService.findDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId);
    }

    @Override
    public void setDiscoveryProtocolPluggableClass(InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass) {
        if (discoveryProtocolPluggableClass!=null) {
            this.discoveryProtocolPluggableClassId = discoveryProtocolPluggableClass.getId();
        } else {
            this.discoveryProtocolPluggableClassId = 0;
        }
    }

    protected void validate() {
        super.validate();
        this.validateDiscoveryProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
    }

    private void validateDiscoveryProtocolPluggableClass(long discoveryProtocolPluggableClassId) {
        if (discoveryProtocolPluggableClassId == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.MUST_HAVE_DISCOVERY_PROTOCOL);
        } else {
            PluggableClass pluggableClass = pluggableService.findDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId);
            if (pluggableClass == null) {
                throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_SUCH_PLUGGABLE_CLASS);
            }
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

            throw new TranslatableApplicationException(thesaurus, MessageSeeds.COMPORTPOOL_STILL_REFERENCED);
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