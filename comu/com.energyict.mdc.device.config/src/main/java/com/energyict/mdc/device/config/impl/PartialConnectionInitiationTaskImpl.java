package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

/**
 *  Provides an implementation for an {@link PartialConnectionInitiationTask}
 *
 *  @author sva
 * @since 22/01/13 - 14:35
 */
public class PartialConnectionInitiationTaskImpl extends PartialScheduledConnectionTaskImpl implements PartialConnectionInitiationTask {

    @Inject
    PartialConnectionInitiationTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.partialConnectionInitiationTaskExists(thesaurus, name);
    }


    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialConnectionInitiationTask.class).remove(this);
    }

//    public void init(final PartialConnectionInitiationTaskShadow shadow) throws SQLException, BusinessException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute () throws BusinessException, SQLException {
//                doInit(shadow);
//                return null;
//            }
//        });
//    }
//
//    protected void doInit(PartialConnectionInitiationTaskShadow shadow) throws SQLException, BusinessException {
//        this.validateNew(shadow);
//        doInitNextExecutionSpecs(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.postProperties(shadow);
//        this.created();
//    }
//
//    private void validate(PartialConnectionInitiationTaskShadow shadow) throws BusinessException {
//        super.validate(shadow);
//        this.validateIsNotDefault(shadow);
//    }
//
//    private void validateIsNotDefault (PartialScheduledConnectionTaskShadow shadow) throws BusinessException {
//        if (shadow.isDefault()) {
//            throw new BusinessException("partialInitiationConnectionTaskCannotBeMarkedAsDefault", "A partial initiation connection task cannot be marked as the default");
//        }
//    }
//
//    private void validateNew(PartialConnectionInitiationTaskShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }
//
//    private void copyNew(PartialConnectionInitiationTaskShadow shadow) {
//        this.copy(shadow);
//    }
//
//    public void update(final PartialConnectionInitiationTaskShadow shadow) throws BusinessException, SQLException {
//        this.execute(new Transaction<Void>() {
//            public Void doExecute () throws BusinessException, SQLException {
//                doUpdate(shadow);
//                return null;
//            }
//        }
//        );
//    }
//
//    private void doUpdate(PartialConnectionInitiationTaskShadow shadow) throws BusinessException, SQLException {
//        this.validateUpdate(shadow);
//        ServerNextExecutionSpecs currentNextExecutionSpecs= this.getNextExecutionSpecs();
//        boolean deleteCurrentNextExecutionSpec = doUpdateNextExecutionSpecs(shadow);
//        this.copyUpdate(shadow);
//        this.post();
//        this.postProperties(shadow);
//        if (deleteCurrentNextExecutionSpec) {
//            currentNextExecutionSpecs.delete();
//        }
//        this.updated();
//    }
//
//    private void copyUpdate(PartialConnectionInitiationTaskShadow shadow) {
//        this.copy(shadow);
//    }
//
//    private void validateUpdate(PartialConnectionInitiationTaskShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }

}
