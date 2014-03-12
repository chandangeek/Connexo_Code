package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

/**
 *  Provides an implementation for an {@link PartialScheduledConnectionTask}
 *
 * @author sva
 * @since 22/01/13 - 11:52
 */
public abstract class PartialScheduledConnectionTaskImpl extends PartialConnectionTaskImpl<OutboundComPortPool> implements PartialScheduledConnectionTask {

    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    /**
     * Defines the minimum reschedule delay of a connectionTask.
     * Values below this must be rejected.
     */
    public static final int MINIMUM_RESCHEDULE_DELAY = 60;

    /**
     * Defines the delay to wait before retrying when this connectionTask failed
     */
    private TimeDuration rescheduleRetryDelay;

    PartialScheduledConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

//    @Override
//    protected void doLoad(ResultSetIterator resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.nextExecutionSpecsId = resultSet.nextInt();
//        final int count = resultSet.nextInt();
//        final int timeUnitCode = resultSet.nextInt();
//        if (count != 0) {
//            this.rescheduleRetryDelay = new TimeDuration(count, timeUnitCode);
//        }
//    }

//    protected DeviceCommunicationConfiguration validate(PartialScheduledConnectionTaskShadow shadow) throws BusinessException {
//        DeviceCommunicationConfiguration configuration = super.validate(shadow);
//        this.validateRescheduleRetryDelay(shadow.getRescheduleDelay());
//        this.validateNextExecutionSpecs(shadow.getNextExecutionSpecs());
//        return configuration;
//    }
//
//    @Override
//    protected boolean validateComPortPoolType (ComPortPool comPortPool) throws InvalidReferenceException {
//        return !comPortPool.isInbound();
//    }

//    protected void doInitNextExecutionSpecs(PartialScheduledConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        boolean autoSchedule = shadow.getNextExecutionSpecs() != null;
//        if (autoSchedule) {
//            this.nextExecutionSpecs = this.getNextExecutionSpecFactory().create(shadow.getNextExecutionSpecs());
//            this.nextExecutionSpecsId = (int) this.nextExecutionSpecs.getId();
//        } else {
//            this.nextExecutionSpecs = null;
//            this.nextExecutionSpecsId = 0;
//        }
//    }

//    protected boolean doUpdateNextExecutionSpecs(PartialScheduledConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        boolean deleteCurrentNextExecutionSpec = false;
//        ServerNextExecutionSpecs currentNextExecutionSpecs = this.getNextExecutionSpecs();
//        if (currentNextExecutionSpecs != null) {
//            if (shadow.getNextExecutionSpecs() == null) {
//                deleteCurrentNextExecutionSpec = true;
//                this.nextExecutionSpecs = null;
//                this.nextExecutionSpecsId = 0;
//            } else {
//                currentNextExecutionSpecs.update(shadow.getNextExecutionSpecs());
//            }
//        } else {
//            this.createNextExecutionSpecsIfAny(shadow);
//        }
//
//        return deleteCurrentNextExecutionSpec;
//    }

//    private boolean createNextExecutionSpecsIfAny(PartialScheduledConnectionTaskShadow shadow) throws BusinessException, SQLException {
//        if (shadow.getNextExecutionSpecs() != null) {
//            this.nextExecutionSpecs = this.getNextExecutionSpecFactory().create(shadow.getNextExecutionSpecs());
//            this.nextExecutionSpecsId = (int) this.nextExecutionSpecs.getId();
//            return true;
//        } else {
//            this.nextExecutionSpecs = null;
//            this.nextExecutionSpecsId = 0;
//            return false;
//        }
//    }

    /**
     * Validate if the rescheduleDelay is a positive integer
     *
     * @param rescheduleRetryDelay the delay to validate
     */
//    private void validateRescheduleRetryDelay(TimeDuration rescheduleRetryDelay) throws BusinessException {
//        if (rescheduleRetryDelay != null) {
//            final int rescheduleDelay = rescheduleRetryDelay.getSeconds();
//            if (rescheduleDelay < 0) {
//                throw new InvalidValueException("invalidNegativeValueForY", "{0} can not be negative", UserEnvironment.getDefault().getTranslation("retryDelay"));
//            } else if (rescheduleDelay != 0 && rescheduleDelay < MINIMUM_RESCHEDULE_DELAY) {
//                throw new BusinessException("invalidValueXForYBelowZ", "{0} is an invalid value for {1}, minimum {2} required",
//                        rescheduleDelay, UserEnvironment.getDefault().getTranslation("retryDelay"), MINIMUM_RESCHEDULE_DELAY);
//            }
//        }
//    }

//    private void validateNextExecutionSpecs(NextExecutionSpecsShadow nextExecutionSpecsShadow) throws BusinessException {
//        if (nextExecutionSpecsShadow != null) {
//            this.validateOffsetNotBiggerThenFrequency(nextExecutionSpecsShadow);
//        }
//    }

//    private void validateOffsetNotBiggerThenFrequency(NextExecutionSpecsShadow nextExecutionSpecs) throws BusinessException {
//        if (this.isNotNull(nextExecutionSpecs.getOffset())) {
//            if (nextExecutionSpecs.getFrequency().getSeconds() < nextExecutionSpecs.getOffset().getSeconds()) {
//                throw new BusinessException(
//                        "OffsetXIsBiggerThanFrequencyY",
//                        "The offset ({0}) should not extend the frequency ({1}",
//                        nextExecutionSpecs.getOffset(),
//                        nextExecutionSpecs.getFrequency());
//            }
//        }
//    }

    private boolean isNotNull(TimeDuration offset) {
        return offset != null && offset.getMilliSeconds() != 0;
    }

//    @Override
//    protected int bindBody(PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
//        parameterNumber = this.bindNextExecutionSpec(preparedStatement, parameterNumber);
//        if (getRescheduleDelay() != null) {
//            preparedStatement.setInt(parameterNumber++, getRescheduleDelay().getCount());
//            preparedStatement.setInt(parameterNumber++, getRescheduleDelay().getTimeUnitCode());
//        } else {
//            preparedStatement.setNull(parameterNumber++, Types.INTEGER);
//            preparedStatement.setNull(parameterNumber++, Types.INTEGER);
//        }
//        return parameterNumber;
//    }

//    private int bindNextExecutionSpec(PreparedStatement preparedStatement, int parameterNumber) throws SQLException {
//        if (this.nextExecutionSpecsId != 0) {
//            preparedStatement.setInt(parameterNumber++, this.nextExecutionSpecsId);
//        } else {
//            preparedStatement.setNull(parameterNumber++, Types.INTEGER);
//        }
//        return parameterNumber;
//    }

    @Override
    public TimeDuration getRescheduleDelay() {
        return rescheduleRetryDelay;
    }

    @Override
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.nextExecutionSpecs.get();
    }

    @Override
    public OutboundComPortPool getComPortPool () {
        return (OutboundComPortPool) super.getComPortPool();
    }

    @Override
    protected Class<OutboundComPortPool> expectedComPortPoolType () {
        return OutboundComPortPool.class;
    }

}
