package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IncompatibleFiniteStateMachineChangeException;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.DecoratedStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Supports switching the {@link FiniteStateMachine}
 * of a set of {@link EndDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-19 (14:37)
 */
public class StateMachineSwitcher implements MessageHandler {

    /**
     * The number of devices that this component will process in one transaction.
     */
    private static final int DEFAULT_BATCH_SIZE = 50;

    private final int batchSize;
    private final DataModel dataModel;
    private Clock clock;
    private EventService eventService;
    private FiniteStateMachineService finiteStateMachineService;
    private JsonService jsonService;
    private Logger logger;

    public static StateMachineSwitcher forValidation(Clock clock, DataModel dataModel) {
        StateMachineSwitcher switcher = new StateMachineSwitcher(dataModel);
        switcher.clock = clock;
        return switcher;
    }

    public static StateMachineSwitcher forPublishing(Clock clock, DataModel dataModel, EventService eventService) {
        StateMachineSwitcher switcher = new StateMachineSwitcher(dataModel);
        switcher.clock = clock;
        switcher.eventService = eventService;
        return switcher;
    }

    public static StateMachineSwitcher forPublishing(int batchSize, Clock clock, DataModel dataModel, EventService eventService) {
        StateMachineSwitcher switcher = new StateMachineSwitcher(dataModel, batchSize);
        switcher.clock = clock;
        switcher.eventService = eventService;
        return switcher;
    }

    public static StateMachineSwitcher forHandling(DataModel dataModel, JsonService jsonService, FiniteStateMachineService finiteStateMachineService, EventService eventService) {
        StateMachineSwitcher switcher = new StateMachineSwitcher(dataModel);
        switcher.jsonService = jsonService;
        switcher.finiteStateMachineService = finiteStateMachineService;
        switcher.eventService = eventService;
        switcher.logger = Logger.getLogger(StateMachineSwitcher.class.getName());
        return switcher;
    }

    private StateMachineSwitcher(DataModel dataModel) {
        this(dataModel, DEFAULT_BATCH_SIZE);
    }

    private StateMachineSwitcher(DataModel dataModel, int batchSize) {
        super();
        this.batchSize = batchSize;
        this.dataModel = dataModel;
    }

    @Override
    public void onMessageDelete(Message message) {
        // Nothing to do for now
    }

    @Override
    public void process(Message message) {
        this.process(this.jsonService.deserialize(message.getPayload(), SwitchStateMachineEvent.class));
    }

    private void process(SwitchStateMachineEvent event) {
        StringTokenizer tokenizer = new StringTokenizer(event.getDeviceIds(), ",");
        while (tokenizer.hasMoreTokens()) {
            long deviceId = Long.parseLong(tokenizer.nextToken());
            Optional<EndDevice> device = this.dataModel.mapper(EndDevice.class).getOptional(deviceId);
            if (device.isPresent()) {
                this.process(event, (ServerEndDevice) device.get());
            }
            else {
                this.logger.fine(() -> "Cannot switch to new statemachine (id=" + event.getNewFiniteStateMachineId() + ") because device with id " + deviceId + " no longer exists");
            }
        }
    }

    private void process(SwitchStateMachineEvent event, ServerEndDevice device) {
        Optional<FiniteStateMachine> stateMachine = this.finiteStateMachineService.findFiniteStateMachineById(event.getNewFiniteStateMachineId());
        if (stateMachine.isPresent()) {
            try {
                device.changeStateMachine(stateMachine.get(), Instant.ofEpochMilli(event.getNow()));
            }
            catch (AbstractEndDeviceImpl.StateNoLongerExistsException e) {
                this.logger.fine(() -> "Cannot switch to new statemachine (id=" + event.getNewFiniteStateMachineId() + ") for device with id " + device.getId() + " because state with name " + e.getStateName() + " has been removed since this was verified");
                // Todo: publish an event
            }
        }
        else {
            this.logger.fine(() -> "Cannot switch to new statemachine (id=" + event.getNewFiniteStateMachineId() + ") for device with id " + device.getId() + " because statemachine no longer exists");
        }
    }

    /**
     * Validates the switch from the old {@link FiniteStateMachine}
     * to the new FiniteStateMachine for the {@link EndDevice}s
     * that match the specified {@link Subquery}.
     * The switch is not valid if the current {@link State}
     * of at least one EndDevice cannot be mapped to a State
     * with the same name in the new FiniteStateMachine.
     *
     * @param oldStateMachine The old FiniteStateMachine
     * @param newStateMachine The new FiniteStateMachine
     * @param deviceAmrIdSubquery The Subquery that returns the set of EndDevice that will be switched
     */
    public void validate(FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery) {
        Instant now = this.clock.instant();
        Set<String> incompatibleStateNames = this.incompatibleStateNames(now, oldStateMachine, newStateMachine, deviceAmrIdSubquery);
        if (!incompatibleStateNames.isEmpty()) {
            /* Need to veto this because there are device out there
             * whose current state no longer exists in the new finite state machine. */
            throw new IncompatibleFiniteStateMachineChangeException(
                    this.toStates(
                            incompatibleStateNames,
                            oldStateMachine));
        }
    }

    private Set<String> incompatibleStateNames(Instant now, FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery) {
        SqlBuilder sqlBuilder = this.incompatibleStateNamesSqlBuilder(now, oldStateMachine, newStateMachine, deviceAmrIdSubquery);
        try (PreparedStatement statement = sqlBuilder.prepare(this.dataModel.getConnection(true))) {
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<String> incompatibleStateNames = new HashSet<>();
                while (resultSet.next()) {
                    incompatibleStateNames.add(resultSet.getString(0));
                }
                return incompatibleStateNames;
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private SqlBuilder incompatibleStateNamesSqlBuilder(Instant now, FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select distinct(s.name) from ");
        sqlBuilder.append(TableSpecs.MTR_ENDDEVICESTATUS.name());
        sqlBuilder.append(" ds join FSM_STATE s on ds.STATE = s.id join ");
        sqlBuilder.append(TableSpecs.MTR_ENDDEVICE.name());
        sqlBuilder.append(" ed on ds.ENDDEVICE = ed.id");
        sqlBuilder.append(" where s.fsm =");
        sqlBuilder.addLong(oldStateMachine.getId());
        sqlBuilder.append("and (ds.STARTTIME <=");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append("and ds.ENDTIME >");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append(") and ed.amrid in (");
        sqlBuilder.add(deviceAmrIdSubquery.toFragment());
        sqlBuilder.append(") and not exists (");
        sqlBuilder.add(new IncompatibleStateNameSqlFragment(newStateMachine));
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private List<State> toStates(Set<String> stateNames, FiniteStateMachine stateMachine) {
        return stateNames
                .stream()
                .map(s -> stateMachine.getState(s).get())
                .collect(Collectors.toList());
    }

    /**
     * Publishes {@link SwitchStateMachineEvent}s to switch the FiniteStateMachine
     * of the {@link EndDevice}s that match the specified {@link Subquery}.
     * These events will be handled in the background by this very component.
     *
     * @param oldStateMachine The old FiniteStateMachine
     * @param newStateMachine The new FiniteStateMachine
     * @param deviceAmrIdSubquery The Subquery that returns the set of EndDevice that will be switched
     */
    public void publishEvents(FiniteStateMachine oldStateMachine, FiniteStateMachine newStateMachine, Subquery deviceAmrIdSubquery) {
        long now = this.clock.instant().toEpochMilli();
        Condition condition =
                where("status").isEffective()
                        .and(where("status.state.finiteStateMachine").isEqualTo(oldStateMachine))
                        .and(ListOperator.IN.contains(deviceAmrIdSubquery, "amrId"))
                        .and(ListOperator.exists(new IncompatibleStateNameSubquery(newStateMachine)).not());
        DecoratedStream.decorate(
                this.dataModel
                    .query(EndDevice.class, EndDeviceLifeCycleStatus.class, State.class)
                    .select(condition)
                    .stream()
                    .map(EndDevice::getId))
                .partitionPer(this.batchSize)
                .map(deviceIds -> new SwitchStateMachineEvent(now, oldStateMachine.getId(), newStateMachine.getId(), deviceIds))
                .forEach(event -> event.publish(this.eventService));
    }

    private static class IncompatibleStateNameSubquery implements Subquery {
        private final FiniteStateMachine stateMachine;

        private IncompatibleStateNameSubquery(FiniteStateMachine stateMachine) {
            super();
            this.stateMachine = stateMachine;
        }

        @Override
        public SqlFragment toFragment() {
            return new IncompatibleStateNameSqlFragment(this.stateMachine);
        }
    }

    private static class IncompatibleStateNameSqlFragment implements SqlFragment {
        private final FiniteStateMachine stateMachine;

        private IncompatibleStateNameSqlFragment(FiniteStateMachine stateMachine) {
            super();
            this.stateMachine = stateMachine;
        }

        @Override
        public int bind(PreparedStatement preparedStatement, int i) throws SQLException {
            preparedStatement.setLong(i, this.stateMachine.getId());
            return i + 1;
        }

        @Override
        public String getText() {
            return "select * from fsm_state where fsm = ? and name = s.name";
        }
    }

}