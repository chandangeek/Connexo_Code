/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@LiteralSql
@Component(name = "com.energyict.mdc.device.config.impl.DeviceTypesLifeCycleCacheChangedEventHandler", service = TopicHandler.class, immediate = true)
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
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        DeviceType deviceType = (DeviceType) localEvent.getSource();
        DataModel dataModel = ormService.getDataModel(DeviceConfigurationService.COMPONENTNAME).get();
        try (Connection connection = dataModel.getConnection(true);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ISU_CREATIONRULEPROPS where NAME IN ('BasicDeviceAlarmRuleTemplate.deviceLifecyleInDeviceTypes'," +
                     "'DeviceLifeCycleInDeviceType.deviceLifecyleInDeviceTypes') AND REGEXP_LIKE(VALUE, '(^|;)" + deviceType.getId() + ":')");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<AlarmRuleTemplateInfo> alarmRuleTemplateInfos = new ArrayList<>();
            while (resultSet.next()) {
                alarmRuleTemplateInfos.add(new AlarmRuleTemplateInfo(resultSet.getLong("CREATIONRULE"), resultSet.getString("VALUE")));
            }
            alarmRuleTemplateInfos.stream()
                    .peek(info -> info.changeValue(deviceType))
                    .forEach(info -> updateDB(info, connection));
            issueService.getIssueCreationService().getCreationRuleQuery()
                    .select(Where.where("id").in(alarmRuleTemplateInfos.stream().map(AlarmRuleTemplateInfo::getId).collect(Collectors.toList())))
                    .forEach(CreationRule::update);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDB(AlarmRuleTemplateInfo info, Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ISU_CREATIONRULEPROPS SET VALUE = '" + info.valuesToString() + "' WHERE CREATIONRULE = " + info.id +
                " AND NAME IN('BasicDeviceAlarmRuleTemplate.deviceLifecyleInDeviceTypes','DeviceLifeCycleInDeviceType.deviceLifecyleInDeviceTypes')")) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    private class AlarmRuleTemplateInfo {
        private long id;
        private List<Value> values;

        public long getId() {
            return id;
        }

        public AlarmRuleTemplateInfo(long id, ArrayList<Value> values) {
            this.id = id;
            this.values = values;
        }

        public AlarmRuleTemplateInfo(long id, String values) {
            this.id = id;
            this.values = parseString(values);
        }

        private List<Value> parseString(String values) {
            return Arrays.stream(values.split(";"))
                    .map(elem -> Arrays.asList(elem.split(":")))
                    .map(e -> {
                        ArrayList<Long> states = new ArrayList<>();
                        Arrays.asList(e.get(2).split(",")).forEach(state -> states.add(Long.parseLong(state)));
                        return new Value(Long.parseLong(e.get(0)), Long.parseLong(e.get(1)), states);
                    })
                    .collect(Collectors.toList());

        }

        public void changeValue(DeviceType deviceType) {
            values.stream()
                    .filter(value -> value.deviceTypeId == deviceType.getId())
                    .forEach(value -> {
                        DeviceLifeCycle oldLifeCycle = deviceLifeCycleConfigurationService.findDeviceLifeCycle(value.lifeCycleId)
                                .orElseThrow(() -> new IllegalStateException("Life cycle with id " + value.lifeCycleId + " no longer exists"));
                        DeviceLifeCycle newLifeCycle = deviceType.getDeviceLifeCycle();
                        value.lifeCycleId = newLifeCycle.getId();
                        if (!changeOldStatesWhichSameAsNew(value, oldLifeCycle, newLifeCycle)) {
                            value.states = deviceType.getDeviceLifeCycle().getFiniteStateMachine().getStates().stream().map(HasId::getId).collect(Collectors.toList());
                        }
                    });
        }

        private boolean changeOldStatesWhichSameAsNew(Value value, DeviceLifeCycle oldLifeCycle, DeviceLifeCycle newLifeCycle) {
            List<State> oldStates = oldLifeCycle.getFiniteStateMachine().getStates().stream()
                    .filter(state -> value.states.contains(state.getId())).collect(Collectors.toList());
            List<State> newStates = newLifeCycle.getFiniteStateMachine().getStates();
            if (newStates.size() < oldStates.size()) {
                return false;
            }
            newStates = newLifeCycle.getFiniteStateMachine().getStates().stream()
                    .filter(state -> oldStates.stream().map(State::getName).anyMatch(stateName -> stateName.equals(state.getName())))
                    .collect(Collectors.toList());
            boolean same = oldStates.size() == newStates.size();
            if (same) {
                value.states = newStates.stream().map(HasId::getId).collect(Collectors.toList());
            }
            return same;
        }

        private class Value {

            private long deviceTypeId;
            private long lifeCycleId;
            private List<Long> states;

            public Value(long deviceTypeId, long lifeCycleId, List<Long> states) {
                this.deviceTypeId = deviceTypeId;
                this.lifeCycleId = lifeCycleId;
                this.states = states;
            }

            public String toString() {
                return deviceTypeId + ":" + lifeCycleId + ":" + states.stream().map(Objects::toString).collect(Collectors.joining(","));
            }

        }

        public String valuesToString() {
            return values.stream().map(Value::toString).collect(Collectors.joining(";"));
        }
    }
}
