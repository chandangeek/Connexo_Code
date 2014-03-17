package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;


/**
 *  Provides an implementation for an {@link PartialOutboundConnectionTask}
 *
 *  @author sva
 * @since 22/01/13 - 17:27
 */
public class PartialOutboundConnectionTaskImpl extends PartialScheduledConnectionTaskImpl implements ServerPartialOutboundConnectionTask {

    private ComWindow comWindow;
    private int comWindowStart;
    private int comWindowEnd;
    private ConnectionStrategy connectionStrategy;
    private boolean allowSimultaneousConnections;
    private Reference<PartialConnectionInitiationTask> initiator = ValueReference.absent();

    @Inject
    PartialOutboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }



//    protected void doInit(PartialOutboundConnectionTaskShadow shadow) throws SQLException, BusinessException {
//        this.validateNew(shadow);
//        doInitNextExecutionSpecs(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.postProperties(shadow);
//        this.created();
//    }

//    private void validateNew(PartialOutboundConnectionTaskShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }

//    private void copyNew(PartialOutboundConnectionTaskShadow shadow) {
//        this.copy(shadow);
//    }

//    @Override
//    protected void doLoad(ResultSetIterator resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.connectionInitiatorId = resultSet.nextInt();
//        this.comWindow = this.loadComWindow(resultSet);
//        this.connectionStrategy = this.toConnectionStrategy(resultSet.nextInt());
//        this.allowSimultaneousConnections = resultSet.nextInt() != 0;
//    }

//    private ComWindow loadComWindow(ResultSetIterator resultSet) throws SQLException {
//        int startSeconds = resultSet.nextInt();
//        if (resultSet.wasNull()) {
//            resultSet.skip();
//            return null;
//        } else {
//            return this.toComWindow(startSeconds, resultSet.nextInt());
//        }
//    }

//    private ComWindow toComWindow(int startSeconds, int endSeconds) {
//        return new ComWindow(startSeconds, endSeconds);
//    }

//    private ConnectionStrategy toConnectionStrategy(int connectionStrategyOrdinalValue) {
//        for (ConnectionStrategy strategy : ConnectionStrategy.values()) {
//            if (strategy.ordinal() == connectionStrategyOrdinalValue) {
//                return strategy;
//            }
//        }
//        return null;
//    }

//    @Override
//    protected int bindBody(PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
//        parameterNumber = this.bindConnectionInitiator(preparedStatement, parameterNumber);
//        parameterNumber = this.bindComWindow(this.comWindow, preparedStatement, parameterNumber);
//        this.bindConnectionStrategy(preparedStatement, parameterNumber++, this.connectionStrategy);
//        preparedStatement.setInt(parameterNumber++, this.toDbValue(this.allowSimultaneousConnections));
//        return parameterNumber;
//    }

//    private int bindConnectionInitiator(PreparedStatement preparedStatement, int parameterNumber) throws SQLException {
//        if (this.connectionInitiatorId != 0) {
//            preparedStatement.setInt(parameterNumber++, this.connectionInitiatorId);
//        } else {
//            preparedStatement.setNull(parameterNumber++, Types.INTEGER);
//        }
//        return parameterNumber;
//    }

//    private int bindComWindow(ComWindow comWindow, PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
//        if (comWindow == null) {
//            preparedStatement.setNull(parameterIndex, Types.INTEGER);
//            preparedStatement.setNull(parameterIndex + 1, Types.INTEGER);
//        } else {
//            preparedStatement.setInt(parameterIndex, this.toSeconds(comWindow.getStart()));
//            preparedStatement.setInt(parameterIndex + 1, this.toSeconds(comWindow.getEnd()));
//        }
//        return parameterIndex + 2;
//    }

    private int toSeconds(PartialTime partialTime) {
        return partialTime.getMillis() / DateTimeConstants.MILLIS_PER_SECOND;
    }

//    private void bindConnectionStrategy(PreparedStatement preparedStatement, int parameterIndex, ConnectionStrategy connectionStrategy) throws SQLException {
//        preparedStatement.setInt(parameterIndex, this.toDbValue(connectionStrategy));
//    }

//    private int toDbValue(ConnectionStrategy connectionStrategy) {
//        return connectionStrategy.ordinal();
//    }

//    protected void validate(PartialOutboundConnectionTaskShadow shadow) throws BusinessException {
//        super.validate(shadow);
//        this.validateNotNull(shadow.getConnectionStrategy(), "connectionStrategy");
//        if (ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(shadow.getConnectionStrategy())) {
//            this.validateNotNull(shadow.getNextExecutionSpecs(), "outboundConnectionTask.nextExecutionSpecs");
//        }
//        if (shadow.getNextExecutionSpecs() != null) {
//            this.validateOffsetWithinComWindow(shadow.getNextExecutionSpecs(), shadow.getCommunicationWindow());
//        }
//        if (shadow.getConnectionInitiationTaskId() != 0) {
//            this.validateConnectionInitiationTask(shadow);
//        }
//    }

//    private void validateConnectionInitiationTask(PartialOutboundConnectionTaskShadow shadow) throws BusinessException {
//        PartialConnectionInitiationTask connectionInitiationTask = findConnectionInitiationTaskForId(shadow.getConnectionInitiationTaskId());
//        if (connectionInitiationTask == null) {
//            throw InvalidReferenceException.newForIdBusinessObject(shadow.getConnectionInitiationTaskId(), ManagerFactory.getCurrent().getPartialConnectionTaskFactory());
//        }
//    }

//    private void validateOffsetWithinComWindow(NextExecutionSpecsShadow nextExecutionSpecs, ComWindow comWindow) throws BusinessException {
//        TimeDuration offset = nextExecutionSpecs.getOffset();
//        if (this.isNotNull(comWindow) && this.isNotNull(offset)) {
//            /* Note that it's possible that the offset is 3 days, 16 hours and 30 min
//            * allowing the communication expert to specify a weekly execution
//            * of the connection task on Wednesday, 16:30:00
//            * So we need to truncate the offset to be within one day.
//            * In the above example we would get 16:30:00 as a result
//            * and we check if that is still within the ComWindow. */
//            TimeDuration offsetWithinDay;
//            if (this.isWithinDay(offset)) {
//                offsetWithinDay = offset;
//            } else {
//                offsetWithinDay = this.truncateToDay(offset);
//            }
//            if (!comWindow.includes(offsetWithinDay) && this.frequencyIsAtLeastADay(nextExecutionSpecs)) {
//                if (this.isWithinDay(offset)) {
//                    throw new BusinessException(
//                            "OffsetXIsNotWithinComWindowY",
//                            "The offset of '{0}' is not within the specified communication window {1}",
//                            offset,
//                            comWindow);
//                } else {
//                    throw new BusinessException(
//                            "LongOffsetXIsNotWithinComWindowY",
//                            "The offset within a week or month of '{0}', resulting in a daily offset of '{1}' is not within the specified communication window {2}",
//                            offset,
//                            offsetWithinDay,
//                            comWindow);
//                }
//            }
//        }
//    }

//    private boolean frequencyIsAtLeastADay(NextExecutionSpecsShadow nextExecutionSpecs) {
//        return nextExecutionSpecs.getFrequency().getSeconds() >= TimeDuration.days(1).getSeconds();
//    }

//    private boolean isNotNull(ComWindow comWindow) {
//        return comWindow != null;
//    }

//    private boolean isNotNull(TimeDuration offset) {
//        return offset != null && offset.getMilliSeconds() != 0;
//    }

//    private boolean isWithinDay(TimeDuration timeDuration) {
//        return timeDuration.getSeconds() <= TimeConstants.SECONDS_IN_DAY;
//    }

//    private TimeDuration truncateToDay(TimeDuration timeDuration) {
//        return new TimeDuration(timeDuration.getSeconds() % TimeConstants.SECONDS_IN_DAY, TimeDuration.SECONDS);
//    }

//    protected void copy(PartialOutboundConnectionTaskShadow shadow) {
//        super.copy(shadow);
//        this.comWindow = shadow.getCommunicationWindow();
//        this.connectionStrategy = shadow.getConnectionStrategy();
//        this.allowSimultaneousConnections = shadow.isSimultaneousConnectionsAllowed();
//        this.connectionInitiatorId = shadow.getConnectionInitiationTaskId();
//    }

    @Override
    public ComWindow getCommunicationWindow() {
        if (comWindow == null) {
            comWindow = new ComWindow(PartialTime.fromSeconds(comWindowStart), PartialTime.fromSeconds(comWindowEnd));
        }
        return this.comWindow;
    }

    public ConnectionStrategy getConnectionStrategy() {
        return connectionStrategy;
    }

//    protected void doUpdate(PartialOutboundConnectionTaskShadow shadow) throws BusinessException, SQLException {
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

//    private void copyUpdate(PartialOutboundConnectionTaskShadow shadow) {
//        this.copy(shadow);
//    }
//
//    private void validateUpdate(PartialOutboundConnectionTaskShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }

    @Override
    public PartialConnectionInitiationTask getInitiatorTask() {
        return initiator.orNull();
    }

    @Override
    public boolean isSimultaneousConnectionsAllowed() {
        return allowSimultaneousConnections;
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.partialOutboundConnectionTaskExists(thesaurus, name);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PARTIAL_OUTBOUND_CONNECTION_TASK;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_OUTBOUND_CONNECTION_TASK;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_OUTBOUND_CONNECTION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialOutboundConnectionTask.class).remove(this);
    }

    public static PartialOutboundConnectionTaskImpl from(DataModel dataModel, ConnectionStrategy connectionStrategy) {
        return dataModel.getInstance(PartialOutboundConnectionTaskImpl.class).init(connectionStrategy);
    }

    private PartialOutboundConnectionTaskImpl init(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
        return this;
    }

    @Override
    public void setComWindow(ComWindow comWindow) {
        this.comWindow = comWindow;
        this.comWindowStart = comWindow.getStart().getMillis();
        this.comWindowEnd = comWindow.getEnd().getMillis();
    }

    @Override
    public void setConnectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    @Override
    public void setAllowSimultaneousConnections(boolean allowSimultaneousConnections) {
        this.allowSimultaneousConnections = allowSimultaneousConnections;
    }
}
