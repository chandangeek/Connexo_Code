/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

@LiteralSql
public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller;
    private final EventService eventService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Logger logger = Logger.getLogger(UpgraderV10_7.class.getName());
    private Map<Long, Pair> campaignIdAndCreationTimeByOldIds = new HashMap<>();
    private Map<Long, Long> campaignStates = new HashMap<>();
    private Map<Long, Long> deviceStates = new HashMap<>();
    private long currentId;
    private long startId;
    private long campaignServiceCallTypeId;
    private long itemServiceCallTypeId;
    private long campaignRegisteredCPSId;
    private long itemRegisteredCPSId;
    private static final long MILLISINDAY = 86400000;

    @Inject
    UpgraderV10_7(DataModel dataModel, FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller,
                  EventService eventService,DeviceConfigurationService deviceConfigurationService) {
        this.dataModel = dataModel;
        this.firmwareCampaignServiceCallLifeCycleInstaller = firmwareCampaignServiceCallLifeCycleInstaller;
        this.eventService = eventService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (Connection connection = dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            firmwareCampaignServiceCallLifeCycleInstaller.createServiceCallTypes();
            execute(statement, "DELETE FROM EVT_EVENTTYPE WHERE COMPONENT = 'FWC'");
            for (EventType eventType : EventType.values()) {
                try {
                    eventType.createIfNotExists(eventService);
                } catch (Exception e) {
                    this.logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            initVariables(statement);
            executeQuery(statement, "SELECT * FROM FWC_CAMPAIGN", this::makeCampaignAndSC);
            executeQuery(statement, "SELECT * FROM FWC_CAMPAIGN_DEVICES", this::makeDeviceAndSC);
            if (startId != currentId) {
                execute(statement, "ALTER SEQUENCE SCS_SERVICE_CALLID INCREMENT BY " + (currentId - startId));
                execute(statement, "SELECT SCS_SERVICE_CALLID.NEXTVAL FROM DUAL");
                execute(statement, "ALTER SEQUENCE SCS_SERVICE_CALLID INCREMENT BY 1");
            }
            execute(statement, "ALTER TABLE FWC_CAMPAIGN_PROPS DROP CONSTRAINT PK_FWC_CAMPAIGN_PROPS DROP INDEX");
            try { // due to different behavior during the upgrade from 10.6 to 10.7 and 10.5 and older to 10.7
                execute(statement, "ALTER TABLE FWC_CAMPAIGN_PROPS DROP CONSTRAINT FK_FWC_PROPS_TO_CAMPAIGN DROP INDEX");
            } catch (Exception ignored) {
            }
            executeQuery(statement, "SELECT * FROM FWC_CAMPAIGN_PROPS", this::updateProps);
            execute(statement, "ALTER TABLE FWC_CAMPAIGN_PROPS ADD CONSTRAINT PK_FWC_CAMPAIGN_PROPS PRIMARY KEY (CAMPAIGN, KEY) USING INDEX");
            dataModelUpgrader.upgrade(dataModel, version(10, 7));
            execute(statement, "UPDATE FWC_CAMPAIGN_PROPS SET CPS_ID = " + campaignRegisteredCPSId);
            execute(statement, "ALTER TABLE FWC_CAMPAIGN_PROPS MODIFY CPS_ID NUMBER NOT NULL");
            execute(statement, "DROP TABLE FWC_CAMPAIGN_STATUS");
            execute(statement, "DROP TABLE FWC_CAMPAIGN_DEVICES");
            execute(statement, "DROP TABLE FWC_CAMPAIGN");
            execute(dataModel, "UPDATE FWC_FIRMWAREVERSION SET TYPE = 3 WHERE TYPE = 2");
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Boolean updateProps(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            execute(dataModel, "UPDATE FWC_CAMPAIGN_PROPS SET CAMPAIGN = " + campaignIdAndCreationTimeByOldIds.get(resultSet.getLong(1)).getFirst() + " WHERE CAMPAIGN=" + resultSet.getLong(1));
        }
        return true;
    }

    private void initVariables(Statement statement) {
        campaignStates.put(0L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.ongoing'", this::toLong));
        campaignStates.put(1L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.successful'", this::toLong));
        campaignStates.put(2L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.cancelled'", this::toLong));
        deviceStates.put(0L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.pending'", this::toLong));
        deviceStates.put(1L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.ongoing'", this::toLong));
        deviceStates.put(2L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.failed'", this::toLong));
        long deviceStateSuccessId = executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.successful'", this::toLong);
        deviceStates.put(3L, deviceStateSuccessId);
        deviceStates.put(4L, deviceStateSuccessId);
        deviceStates.put(5L, deviceStateSuccessId);
        deviceStates.put(6L, deviceStateSuccessId);
        deviceStates.put(7L, deviceStateSuccessId);
        deviceStates.put(8L, deviceStateSuccessId);
        deviceStates.put(9L, deviceStateSuccessId);
        deviceStates.put(10L, deviceStateSuccessId);
        deviceStates.put(11L, deviceStateSuccessId);
        deviceStates.put(12L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.rejected'", this::toLong));
        deviceStates.put(13L, executeQuery(statement, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.cancelled'", this::toLong));
        currentId = executeQuery(statement, "SELECT NVL(MAX(ID),0) FROM SCS_SERVICE_CALL", this::toLong);
        startId = currentId;
        campaignServiceCallTypeId = executeQuery(statement, "SELECT \"ID\" FROM \"SCS_SERVICE_CALL_TYPE\" WHERE \"HANDLER\"='FirmwareCampaignServiceCallHandler'", this::toLong);
        itemServiceCallTypeId = executeQuery(statement, "SELECT \"ID\" FROM \"SCS_SERVICE_CALL_TYPE\" WHERE \"HANDLER\"='FirmwareCampaignItemServiceCallHandler'", this::toLong);
        campaignRegisteredCPSId = executeQuery(statement, "SELECT \"ID\" FROM \"CPS_REGISTERED_CUSTOMPROPSET\" WHERE \"LOGICALID\"='com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension'", this::toLong);
        itemRegisteredCPSId = executeQuery(statement, "SELECT \"ID\" FROM \"CPS_REGISTERED_CUSTOMPROPSET\" WHERE \"LOGICALID\"='com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemDomainExtension'", this::toLong);
    }

    private Boolean makeCampaignAndSC(ResultSet resultSet) throws SQLException {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        List<FirmwareCampaignInfo> firmwareCampaignInfos = new ArrayList<>();
        while (resultSet.next()) {
            campaignIdAndCreationTimeByOldIds.put(resultSet.getLong("ID"), Pair.of(++currentId, resultSet.getLong("CREATETIME")));
            serviceCallInfos.add(makeCampaignSC(resultSet));
            firmwareCampaignInfos.add(makeCampaign(resultSet));
        }
        if (!firmwareCampaignInfos.isEmpty()) {
            serviceCallInfos.forEach(this::writeScInBd);
            firmwareCampaignInfos.forEach(this::writeCampaignInBd);
        }
        return true;
    }

    private Boolean makeDeviceAndSC(ResultSet resultSet) throws SQLException {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        List<FirmwareCampaignItemInfo> firmwareCampaignItemInfos = new ArrayList<>();
        while (resultSet.next()) {
            currentId++;
            serviceCallInfos.add(makeDeviceSC(resultSet));
            firmwareCampaignItemInfos.add(makeDevice(resultSet));
        }
        if (!firmwareCampaignItemInfos.isEmpty()) {
            serviceCallInfos.forEach(this::writeScInBd);
            firmwareCampaignItemInfos.forEach(this::writeCampaignItemInBd);
        }
        return true;
    }

    private void writeScInBd(ServiceCallInfo info) {
        String values = new ValueBuilder()
                .add(info.id)
                .add(info.parent)
                .add(info.lastCompletedTime)
                .add(info.state)
                .add(info.origin)
                .add(info.externalReference)
                .add(info.targetCmp)
                .add(info.targetTable)
                .add(info.targetKey)
                .add(info.targetId)
                .add(info.serviceCallType)
                .add(info.versionCount)
                .add(info.createTime)
                .add(info.modTime)
                .add(info.username)
                .end();
        execute(dataModel, "INSERT INTO \"SCS_SERVICE_CALL\" (ID,PARENT,LASTCOMPLETEDTIME,STATE,ORIGIN,EXTERNALREFERENCE,TARGETCMP,TARGETTABLE,TARGETKEY,TARGETID,SERVICECALLTYPE,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME) VALUES " + values);
    }

    private void writeCampaignInBd(FirmwareCampaignInfo info) {
        String values = new ValueBuilder()
                .add(info.serviceCall)
                .add(info.cps)
                .add(info.versionCount)
                .add(info.createTime)
                .add(info.modTime)
                .add(info.username)
                .add(info.name)
                .add(info.deviceGroup)
                .add(info.managementOption)
                .add(info.firmwareType)
                .add(info.activationStart)
                .add(info.activationEnd)
                .add(info.deviceType)
                .add(info.activationDate)
                .add(info.validationTimeoutValue)
                .add(info.validationTimeoutUnit)
                .add(info.firmwareUploadComTaskId)
                .add(info.validationComTaskId)
                .end();
        execute(dataModel, "INSERT INTO FWC_FC1_CAMPAIGN (SERVICECALL,CPS,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME,NAME,DEVICE_GROUP,MANAGEMENT_OPTION,FIRMWARE_TYPE,ACTIVATION_START,ACTIVATION_END,DEVICE_TYPE,ACTIVATION_DATE,VALIDATION_TIMEOUT_VALUE,VALIDATION_TIMEOUT_UNIT,FIRMWARE_UPLOAD_COMTASK_ID,VALIDATION_COMTASK_ID) VALUES " + values);
    }

    private void writeCampaignItemInBd(FirmwareCampaignItemInfo info) {
        String values = new ValueBuilder()
                .add(info.serviceCall)
                .add(info.cps)
                .add(info.versionCount)
                .add(info.createTime)
                .add(info.modTime)
                .add(info.username)
                .add(info.parent)
                .add(info.device)
                .add(info.deviceMessage)
                .end();
        execute(dataModel, "INSERT INTO FWC_FC2_ITEMS (SERVICECALL,CPS,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME,PARENT,DEVICE,DEVICE_MESSAGE_ID) VALUES " + values);
    }

    private ServiceCallInfo makeCampaignSC(ResultSet resultSet) throws SQLException {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = currentId;
        serviceCallInfo.parent = null;
        serviceCallInfo.lastCompletedTime = null;
        serviceCallInfo.state = campaignStates.get(resultSet.getLong("STATUS"));
        serviceCallInfo.origin = "MultiSense";
        serviceCallInfo.externalReference = null;
        serviceCallInfo.targetCmp = null;
        serviceCallInfo.targetTable = null;
        serviceCallInfo.targetKey = null;
        serviceCallInfo.targetId = null;
        serviceCallInfo.serviceCallType = campaignServiceCallTypeId;
        serviceCallInfo.versionCount = resultSet.getLong("VERSIONCOUNT");
        serviceCallInfo.createTime = resultSet.getLong("CREATETIME");
        serviceCallInfo.modTime = resultSet.getLong("MODTIME");
        serviceCallInfo.username = resultSet.getString("USERNAME");
        return serviceCallInfo;
    }

    private ServiceCallInfo makeDeviceSC(ResultSet resultSet) throws SQLException {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = currentId;
        serviceCallInfo.parent = (Long) campaignIdAndCreationTimeByOldIds.get(resultSet.getLong("CAMPAIGN")).getFirst();
        serviceCallInfo.lastCompletedTime = null;
        serviceCallInfo.state = deviceStates.get(resultSet.getLong("STATUS"));
        serviceCallInfo.origin = null;
        serviceCallInfo.externalReference = null;
        serviceCallInfo.targetCmp = "DDC";
        serviceCallInfo.targetTable = "DDC_DEVICE";
        long deviceId = resultSet.getLong("DEVICE");
        serviceCallInfo.targetKey = "[" + deviceId + "]";
        serviceCallInfo.targetId = deviceId;
        serviceCallInfo.serviceCallType = itemServiceCallTypeId;
        serviceCallInfo.versionCount = 1L;
        serviceCallInfo.createTime = resultSet.getLong("STARTED_ON") == 0 ? (Long) campaignIdAndCreationTimeByOldIds.get(resultSet.getLong("CAMPAIGN")).getLast() : resultSet.getLong("STARTED_ON");
        serviceCallInfo.modTime = resultSet.getLong("FINISHED_ON");
        serviceCallInfo.username = "batch executor";
        return serviceCallInfo;
    }

    private FirmwareCampaignInfo makeCampaign(ResultSet resultSet) throws SQLException {
        FirmwareCampaignInfo firmwareCampaignInfo = new FirmwareCampaignInfo();
        firmwareCampaignInfo.serviceCall = currentId;
        firmwareCampaignInfo.cps = campaignRegisteredCPSId;
        firmwareCampaignInfo.versionCount = resultSet.getLong("VERSIONCOUNT");
        firmwareCampaignInfo.createTime = resultSet.getLong("CREATETIME");
        firmwareCampaignInfo.modTime = resultSet.getLong("MODTIME");
        firmwareCampaignInfo.username = resultSet.getString("USERNAME");
        firmwareCampaignInfo.name = resultSet.getString("CAMPAIGN_NAME");
        firmwareCampaignInfo.deviceGroup = " ";
        firmwareCampaignInfo.managementOption = resultSet.getLong("MANAGEMENT_OPTION");
        firmwareCampaignInfo.firmwareType = resultSet.getLong("FIRMWARE_TYPE");
        long dayOfStart = LocalDate.ofEpochDay(firmwareCampaignInfo.createTime / MILLISINDAY)
                .atStartOfDay()
                .toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
                .getEpochSecond() * 1000;
        firmwareCampaignInfo.activationStart = dayOfStart + resultSet.getLong("COMWINDOWSTART");
        firmwareCampaignInfo.activationEnd = dayOfStart + resultSet.getLong("COMWINDOWEND");
        firmwareCampaignInfo.deviceType = resultSet.getLong("DEVICE_TYPE");
        firmwareCampaignInfo.activationDate = executeQuery(dataModel, "SELECT \"VALUE\" FROM FWC_CAMPAIGN_PROPS WHERE \"KEY\"='FirmwareDeviceMessage.upgrade.activationdate' AND \"CAMPAIGN\"=" + resultSet
                .getLong("ID"), this::toLong);
        firmwareCampaignInfo.validationTimeoutValue = resultSet.getLong("VALIDATION_TIMEOUT_VALUE");
        firmwareCampaignInfo.validationTimeoutUnit = resultSet.getLong("VALIDATION_TIMEOUT_UNIT");

        firmwareCampaignInfo.firmwareUploadComTaskId = deviceConfigurationService.findDeviceType(firmwareCampaignInfo.deviceType)
                .map(deviceType -> deviceType.getConfigurations().stream()
                        .flatMap(cnf -> cnf.getComTaskEnablements().stream())
                        .filter(cte -> cte.getComTask().getName().equals(TaskService.FIRMWARE_COMTASK_NAME) && !cte.isSuspended())
                        .findAny()
                        .map(cte -> cte.getComTask().getId())
                        .orElse(null))
                .orElse(null);

        firmwareCampaignInfo.validationComTaskId = deviceConfigurationService.findDeviceType(firmwareCampaignInfo.deviceType)
                .map(deviceType -> deviceType.getConfigurations().stream()
                        .flatMap(cnf -> cnf.getComTaskEnablements().stream())
                        .flatMap(cte -> {
                            if (!cte.isSuspended()) {
                                return cte.getComTask().getProtocolTasks().stream();
                            } else {
                                return new ArrayList<ProtocolTask>().stream();
                            }
                        })
                        .filter(protocolTask -> protocolTask instanceof StatusInformationTask)
                        .findAny()
                        .map(cte -> cte.getComTask().getId())
                        .orElse(null))
                .orElse(null);

        return firmwareCampaignInfo;
    }

    private FirmwareCampaignItemInfo makeDevice(ResultSet resultSet) throws SQLException {
        FirmwareCampaignItemInfo firmwareCampaignItemInfo = new FirmwareCampaignItemInfo();
        firmwareCampaignItemInfo.serviceCall = currentId;
        firmwareCampaignItemInfo.cps = itemRegisteredCPSId;
        firmwareCampaignItemInfo.versionCount = 1L;
        firmwareCampaignItemInfo.createTime = resultSet.getLong("STARTED_ON");
        firmwareCampaignItemInfo.modTime = resultSet.getLong("FINISHED_ON");
        firmwareCampaignItemInfo.username = "batch executor";
        firmwareCampaignItemInfo.parent = (Long) campaignIdAndCreationTimeByOldIds.get(resultSet.getLong("CAMPAIGN")).getFirst();
        firmwareCampaignItemInfo.device = resultSet.getLong("DEVICE");
        firmwareCampaignItemInfo.deviceMessage = resultSet.getObject("MESSAGE_ID", Long.class);
        return firmwareCampaignItemInfo;
    }

    private Long toLong(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return null;
        }
    }

    private class ValueBuilder {
        private String values = "(";

        private ValueBuilder add(String value) {
            if (value != null) {
                values += "'" + value + "',";
            } else {
                values += "null,";
            }
            return this;
        }

        private ValueBuilder add(Long value) {
            if (value != null) {
                values += "'" + value + "',";
            } else {
                values += "null,";
            }
            return this;
        }

        private String end() {
            return values.substring(0, values.length() - 1) + ")";
        }
    }

    private class FirmwareCampaignInfo {
        Long serviceCall;
        Long cps;
        Long versionCount;
        Long createTime;
        Long modTime;
        String username;
        String name;
        String deviceGroup;
        Long managementOption;
        Long firmwareType;
        Long activationStart;
        Long activationEnd;
        Long deviceType;
        Long activationDate;
        Long validationTimeoutValue;
        Long validationTimeoutUnit;
        Long firmwareUploadComTaskId;
        Long validationComTaskId;
    }

    private class ServiceCallInfo {
        Long id;
        Long parent;
        Long lastCompletedTime;
        Long state;
        String origin;
        String externalReference;
        String targetCmp;
        String targetTable;
        String targetKey;
        Long targetId;
        Long serviceCallType;
        Long versionCount;
        Long createTime;
        Long modTime;
        String username;
    }

    private class FirmwareCampaignItemInfo {
        Long serviceCall;
        Long cps;
        Long versionCount;
        Long createTime;
        Long modTime;
        String username;
        Long parent;
        Long device;
        Long deviceMessage;
    }

}
