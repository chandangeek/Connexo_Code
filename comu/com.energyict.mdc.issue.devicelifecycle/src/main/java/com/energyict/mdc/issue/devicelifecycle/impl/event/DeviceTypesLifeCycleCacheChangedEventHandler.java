/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@LiteralSql
@Component(name = "com.energyict.mdc.device.alarms.impl.event.DeviceTypesLifeCycleCacheChangedEventHandler", service = TopicHandler.class, immediate = true)
public class DeviceTypesLifeCycleCacheChangedEventHandler implements TopicHandler {
    private static final String TOPIC = "com/energyict/mdc/device/config/devicetype/LIFE_CYCLE_CACHE_RECALCULATED";
    private volatile IssueService issueService;
    private volatile OrmService ormService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public DeviceTypesLifeCycleCacheChangedEventHandler() {
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setIssueDeviceLifecycleService(IssueDeviceLifecycleService issueDeviceLifecycleService) {
        //  to make sure that datamodel is initialized
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        DataModel dataModel = ormService.getDataModel(IssueDeviceLifecycleService.COMPONENT_NAME).get();
        try (Connection connection = dataModel.getConnection(true);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ISU_CREATIONRULEPROPS where NAME = 'DeviceLifecycleIssueCreationRuleTemplate.deviceLifecycleTransitionProps' " +
                     "AND REGEXP_LIKE(VALUE, '(^|;)" + deviceType.getId() + ":')");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<IssueRuleTemplateInfo> issueRuleTemplateInfos = new ArrayList<>();
            while (resultSet.next()) {
                issueRuleTemplateInfos.add(new IssueRuleTemplateInfo(resultSet.getLong("CREATIONRULE"), resultSet.getString("VALUE")));
            }
            issueRuleTemplateInfos.forEach(info -> info.changeValue(deviceType));
            issueRuleTemplateInfos.forEach(info -> updateBD(info, connection));
            issueService.getIssueCreationService().getCreationRuleQuery()
                    .select(Where.where("id").in(issueRuleTemplateInfos.stream().map(IssueRuleTemplateInfo::getId).collect(Collectors.toList())))
                    .forEach(CreationRule::update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBD(IssueRuleTemplateInfo info, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ISU_CREATIONRULEPROPS SET VALUE = '" + info.valuesToString() + "' WHERE CREATIONRULE = " + info.id +
                "AND NAME = 'DeviceLifecycleIssueCreationRuleTemplate.deviceLifecycleTransitionProps'")) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    class IssueRuleTemplateInfo {
        long id;
        List<Value> values;

        public long getId() {
            return id;
        }

        public IssueRuleTemplateInfo(long id, ArrayList<Value> values) {
            this.id = id;
            this.values = values;
        }

        public IssueRuleTemplateInfo(long id, String values) {
            this.id = id;
            this.values = parseString(values);
        }

        private List<Value> parseString(String values) {
            return Arrays.stream(values.split(";"))
                    .map(elem -> Arrays.asList(elem.split(":")))
                    .map(e -> new Value(Long.parseLong(e.get(0)), Long.parseLong(e.get(1)), Long.parseLong(e.get(2)), Long.parseLong(e.get(3).split("-")[0]), Long.parseLong(e.get(3).split("-")[1])))
                    .collect(Collectors.toList());
        }

        public void changeValue(DeviceType deviceType) {
            DeviceLifeCycle oldLifeCycle = deviceLifeCycleConfigurationService.findDeviceLifeCycle(this.values.get(0).lifeCycleId).orElseThrow(() -> new IllegalStateException("wtf"));
            DeviceLifeCycle newLifeCycle = deviceType.getDeviceLifeCycle();
            if (!allOldTransitionsAreSameAsNew(this.values, oldLifeCycle, newLifeCycle, deviceType)) {
                this.values.removeIf(value -> value.deviceTypeId == deviceType.getId());
                newLifeCycle.getFiniteStateMachine()
                        .getTransitions()
                        .forEach(transition -> this.values.add(new Value(deviceType.getId(), newLifeCycle.getId(), transition.getId(), transition.getFrom().getId(), transition.getTo().getId())));
            }

        }

        private boolean allOldTransitionsAreSameAsNew(List<Value> values, DeviceLifeCycle oldLifeCycle, DeviceLifeCycle newLifeCycle, DeviceType deviceType) {
            List<StateTransition> oldStateTransitions = oldLifeCycle.getFiniteStateMachine().getTransitions().stream()
                    .filter(transition -> values.stream().anyMatch(value -> value.transition == transition.getId())).collect(Collectors.toList());
            List<StateTransition> newStatesTransitions = newLifeCycle.getFiniteStateMachine().getTransitions();
            if (newStatesTransitions.size() > oldStateTransitions.size()) {
                return false;
            }
            newStatesTransitions = newLifeCycle.getFiniteStateMachine().getTransitions().stream()
                    .map(stateTransition -> {
                        List<Pair<StateTransition, StateTransition>> pairs = new ArrayList<>();
                        oldStateTransitions.stream().filter(oldState -> oldState.getEventType().equals(stateTransition.getEventType()))
                                .forEach(stateTransition1 -> pairs.add(Pair.of(stateTransition, stateTransition1)));
                        return pairs;
                    })
                    .flatMap(List::stream)
                    .filter(element -> element.getLast() != null)
                    .filter(element -> element.getFirst().getFrom().getName().equals(element.getLast().getFrom().getName()))
                    .filter(element -> element.getFirst().getTo().getName().equals(element.getLast().getTo().getName()))
                    .map(Pair::getFirst)
                    .collect(Collectors.toList());
            boolean same = oldStateTransitions.size() == newStatesTransitions.size();
            if (same) {
                this.values.removeIf(value -> value.deviceTypeId == deviceType.getId());
                newStatesTransitions.forEach(transition -> this.values.add(new Value(deviceType.getId(), newLifeCycle.getId(), transition.getId(), transition.getFrom().getId(), transition.getTo()
                        .getId())));
            }
            return same;
        }

        class Value {

            long deviceTypeId;
            long lifeCycleId;
            long transition;
            long fromState;
            long toState;

            public Value(long deviceTypeId, long lifeCycleId, long transition, long fromState, long toState) {
                this.deviceTypeId = deviceTypeId;
                this.lifeCycleId = lifeCycleId;
                this.transition = transition;
                this.fromState = fromState;
                this.toState = toState;
            }

            public String toString() {
                return deviceTypeId + ":" + lifeCycleId + ":" + transition + ":" + fromState + "-" + toState;
            }

        }

        public String valuesToString() {
            return values.stream().map(Value::toString).collect(Collectors.joining(";"));
        }
    }
}
