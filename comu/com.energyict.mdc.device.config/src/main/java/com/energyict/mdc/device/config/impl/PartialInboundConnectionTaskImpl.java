package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

/**
 * Provides an implementation for an {@link PartialInboundConnectionTask}
 *
 * @author sva
 * @since 21/01/13 - 16:43
 */
public class PartialInboundConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialInboundConnectionTask {

    @Inject
    PartialInboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

    static PartialInboundConnectionTask from(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        return dataModel.getInstance(PartialInboundConnectionTaskImpl.class).init(configuration);
    }

    private PartialInboundConnectionTask init(DeviceCommunicationConfiguration configuration) {
        setConfiguration(configuration);
        return this;
    }


//    public void init(final PartialInboundConnectionTaskShadow shadow) throws SQLException, BusinessException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute () throws BusinessException, SQLException {
//                doInit(shadow);
//                return null;
//            }
//        });
//    }

//    private void doInit(PartialInboundConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        this.validateNew(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.postProperties(shadow);
//        this.created();
//    }

//    private void validateNew(PartialInboundConnectionTaskShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }

//    @Override
//    protected boolean validateComPortPoolType (ComPortPool comPortPool) throws InvalidReferenceException {
//        return comPortPool.isInbound();
//    }

//    private void copyNew(PartialInboundConnectionTaskShadow shadow) {
//        copy(shadow);
//    }

//    @Override
//    public void update(final PartialInboundConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute() throws BusinessException, SQLException {
//                doUpdate(shadow);
//                return null;
//            }
//        });
//    }

//    private void doUpdate(PartialInboundConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        this.validateUpdate(shadow);
//        this.copyUpdate(shadow);
//        this.post();
//        this.postProperties(shadow);
//        this.updated();
//    }

//    private void validateUpdate(PartialInboundConnectionTaskShadow shadow) throws BusinessException {
//        validate(shadow);
//    }
//
//    private void copyUpdate(PartialInboundConnectionTaskShadow shadow) {
//        copy(shadow);
//    }

    @Override
    protected final CreateEventType createEventType() {
        return CreateEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected final UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected final DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialInboundConnectionTask.class).remove(this);
    }

    @Override
    public InboundComPortPool getComPortPool () {
        return (InboundComPortPool) super.getComPortPool();
    }

    @Override
    public void setComportPool(InboundComPortPool comPortPool) {
        doSetComportPool(comPortPool);
    }

    @Override
    protected Class<InboundComPortPool> expectedComPortPoolType () {
        return InboundComPortPool.class;
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.partialInboundConnectionTaskExists(thesaurus, name);
    }
}
