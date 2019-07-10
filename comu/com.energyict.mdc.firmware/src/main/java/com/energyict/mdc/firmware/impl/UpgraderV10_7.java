/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller;
    Map<Long, Long> campaignIds = new HashMap<>();
    Map<Long, Long> campaignStates = new HashMap<>();
    Map<Long, Long> deviceStates = new HashMap<>();
    long currentId;
    long scsCampaignTypeId;
    long scsCampaignItemTypeId;
    long cpsCampaign;
    long cpsCampaignItem;
    static final long SECONDSINDAY = 86400;

    @Inject
    UpgraderV10_7(DataModel dataModel, FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller) {
        this.dataModel = dataModel;
        this.firmwareCampaignServiceCallLifeCycleInstaller = firmwareCampaignServiceCallLifeCycleInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        firmwareCampaignServiceCallLifeCycleInstaller.createServiceCallTypes();
        initVariables();
        executeQuery(dataModel, "SELECT * FROM FWC_CAMPAIGN", this::makeCampaignAndSC);
        executeQuery(dataModel, "SELECT * FROM FWC_CAMPAIGN_DEVICES", this::makeDeviceAndSC);
        execute(dataModel, "ALTER TABLE FWC_CAMPAIGN_PROPS DROP CONSTRAINT PK_FWC_CAMPAIGN_PROPS DROP INDEX",
                "ALTER TABLE FWC_CAMPAIGN_PROPS DROP CONSTRAINT FK_FWC_PROPS_TO_CAMPAIGN DROP INDEX");
        executeQuery(dataModel, "SELECT * FROM FWC_CAMPAIGN_PROPS", this::updateProps);
        execute(dataModel, "ALTER TABLE FWC_CAMPAIGN_PROPS ADD CONSTRAINT PK_FWC_CAMPAIGN_PROPS PRIMARY KEY (CAMPAIGN, KEY) USING INDEX");
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        execute(dataModel, "UPDATE FWC_CAMPAIGN_PROPS SET CPS_ID = " + cpsCampaign,
                "ADD CONSTRAINT FK_FWC_PROPS_TO_CAMPAIGN FOREIGN KEY (CAMPAIGN, CPS_ID) REFERENCES FWC_FC1_CAMPAIGN(SERVICECALL,CPS) ON DELETE CASCADE",
                "CREATE INDEX FK_FWC_PROPS_TO_CAMPAIGN ON FWC_CAMPAIGN_PROPS (CAMPAIGN, CPS_ID)");
        execute(dataModel, "DROP TABLE FWC_CAMPAIGN_STATUS");
        execute(dataModel, "DROP TABLE FWC_CAMPAIGN_DEVICES");
        execute(dataModel, "DROP TABLE FWC_CAMPAIGN");
    }

    private Boolean updateProps(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            execute(dataModel, "UPDATE FWC_CAMPAIGN_PROPS SET CAMPAIGN = " + campaignIds.get(resultSet.getLong(1)) + " WHERE CAMPAIGN=" + resultSet.getLong(1));
        }
        return true;
    }

    private void initVariables() {
        campaignStates.put(0L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.ongoing'", this::toLong));
        campaignStates.put(1L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.successful'", this::toLong));
        campaignStates.put(2L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaign') AND \"NAME\"='sclc.default.cancelled'", this::toLong));
        deviceStates.put(0L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.pending'", this::toLong));
        deviceStates.put(1L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.ongoing'", this::toLong));
        deviceStates.put(2L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.failed'", this::toLong));
        long deviceStateSuccessId = executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.successful'", this::toLong);
        deviceStates.put(3L, deviceStateSuccessId);
        deviceStates.put(4L, deviceStateSuccessId);
        deviceStates.put(5L, deviceStateSuccessId);
        deviceStates.put(6L, deviceStateSuccessId);
        deviceStates.put(7L, deviceStateSuccessId);
        deviceStates.put(8L, deviceStateSuccessId);
        deviceStates.put(9L, deviceStateSuccessId);
        deviceStates.put(10L, deviceStateSuccessId);
        deviceStates.put(11L, deviceStateSuccessId);
        deviceStates.put(12L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.rejected'", this::toLong));
        deviceStates.put(13L, executeQuery(dataModel, "SELECT \"ID\" FROM \"FSM_STATE\" WHERE \"FSM\" = (SELECT \"ID\" FROM \"FSM_FINITE_STATE_MACHINE\" WHERE \"NAME\"='FirmwareCampaignItem') AND \"NAME\"='sclc.default.cancelled'", this::toLong));
        currentId = executeQuery(dataModel, "SELECT NVL(MAX(ID),0) FROM SCS_SERVICE_CALL", this::toLong);
        scsCampaignTypeId = executeQuery(dataModel, "SELECT \"ID\" FROM \"SCS_SERVICE_CALL_TYPE\" WHERE \"HANDLER\"='FirmwareCampaignServiceCallHandler'", this::toLong);
        scsCampaignItemTypeId = executeQuery(dataModel, "SELECT \"ID\" FROM \"SCS_SERVICE_CALL_TYPE\" WHERE \"HANDLER\"='FirmwareCampaignItemServiceCallHandler'", this::toLong);
        cpsCampaign = executeQuery(dataModel, "SELECT \"ID\" FROM \"CPS_REGISTERED_CUSTOMPROPSET\" WHERE \"LOGICALID\"='com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension'", this::toLong);
        cpsCampaignItem = executeQuery(dataModel, "SELECT \"ID\" FROM \"CPS_REGISTERED_CUSTOMPROPSET\" WHERE \"LOGICALID\"='com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemDomainExtension'", this::toLong);
    }

    private Boolean makeCampaignAndSC(ResultSet resultSet) throws SQLException {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        List<FirmwareCampaignInfo> firmwareCampaignInfos = new ArrayList<>();
        while (resultSet.next()) {
            campaignIds.put(resultSet.getLong(1), ++currentId);
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
            firmwareCampaignItemInfos.forEach(this::writeCampaignItemsInBd);
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
                .end();
        execute(dataModel, "INSERT INTO FWC_FC1_CAMPAIGN (SERVICECALL,CPS,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME,NAME,DEVICE_GROUP,MANAGEMENT_OPTION,FIRMWARE_TYPE,ACTIVATION_START,ACTIVATION_END,DEVICE_TYPE,ACTIVATION_DATE,VALIDATION_TIMEOUT_VALUE,VALIDATION_TIMEOUT_UNIT) VALUES " + values);
    }

    private void writeCampaignItemsInBd(FirmwareCampaignItemInfo info) {
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
        serviceCallInfo.state = campaignStates.get(resultSet.getLong(3));
        serviceCallInfo.origin = "MultiSense";
        serviceCallInfo.externalReference = null;
        serviceCallInfo.reference = new DecimalFormat("SC_00000000").format(currentId);
        serviceCallInfo.targetCmp = null;
        serviceCallInfo.targetTable = null;
        serviceCallInfo.targetKey = null;
        serviceCallInfo.targetId = null;
        serviceCallInfo.serviceCallType = scsCampaignTypeId;
        serviceCallInfo.versionCount = resultSet.getLong(14);
        serviceCallInfo.createTime = resultSet.getLong(15);
        serviceCallInfo.modTime = resultSet.getLong(16);
        serviceCallInfo.username = resultSet.getString(17);
        return serviceCallInfo;
    }

    private ServiceCallInfo makeDeviceSC(ResultSet resultSet) throws SQLException {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = currentId;
        serviceCallInfo.parent = campaignIds.get(resultSet.getLong(1));
        serviceCallInfo.lastCompletedTime = null;
        serviceCallInfo.state = deviceStates.get(resultSet.getLong(3));
        serviceCallInfo.origin = null;
        serviceCallInfo.externalReference = null;
        serviceCallInfo.reference = new DecimalFormat("SC_00000000").format(currentId);
        serviceCallInfo.targetCmp = "DDC";
        serviceCallInfo.targetTable = "DDC_DEVICE";
        long deviceId = resultSet.getLong(2);
        serviceCallInfo.targetKey = "[" + deviceId + "]";
        serviceCallInfo.targetId = deviceId;
        serviceCallInfo.serviceCallType = scsCampaignItemTypeId;
        serviceCallInfo.versionCount = 1L;
        serviceCallInfo.createTime = resultSet.getLong(5);
        serviceCallInfo.modTime = resultSet.getLong(6);
        serviceCallInfo.username = "batch executor";
        return serviceCallInfo;
    }

    private FirmwareCampaignInfo makeCampaign(ResultSet resultSet) throws SQLException {
        FirmwareCampaignInfo firmwareCampaignInfo = new FirmwareCampaignInfo();
        firmwareCampaignInfo.serviceCall = currentId;
        firmwareCampaignInfo.cps = cpsCampaign;
        firmwareCampaignInfo.versionCount = resultSet.getLong(14);
        firmwareCampaignInfo.createTime = resultSet.getLong(15);
        firmwareCampaignInfo.modTime = resultSet.getLong(16);
        firmwareCampaignInfo.username = resultSet.getString(17);
        firmwareCampaignInfo.name = resultSet.getString(2);
        firmwareCampaignInfo.deviceGroup = " ";
        firmwareCampaignInfo.managementOption = resultSet.getLong(5);
        firmwareCampaignInfo.firmwareType = resultSet.getLong(6);
        long dayOfStart = LocalDate.ofEpochDay(firmwareCampaignInfo.createTime / SECONDSINDAY).atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond() * 1000;
        firmwareCampaignInfo.activationStart = dayOfStart + resultSet.getLong(9);
        firmwareCampaignInfo.activationEnd = dayOfStart + resultSet.getLong(10);
        firmwareCampaignInfo.deviceType = resultSet.getLong(4);
        firmwareCampaignInfo.activationDate = executeQuery(dataModel, "SELECT \"VALUE\" FROM FWC_CAMPAIGN_PROPS WHERE \"KEY\"='FirmwareDeviceMessage.upgrade.activationdate' AND \"CAMPAIGN\"=" + resultSet
                .getLong(1), this::toLong);
        firmwareCampaignInfo.validationTimeoutValue = resultSet.getLong(12);
        firmwareCampaignInfo.validationTimeoutUnit = resultSet.getLong(13);
        return firmwareCampaignInfo;
    }

    private FirmwareCampaignItemInfo makeDevice(ResultSet resultSet) throws SQLException {
        FirmwareCampaignItemInfo firmwareCampaignItemInfo = new FirmwareCampaignItemInfo();
        firmwareCampaignItemInfo.serviceCall = currentId;
        firmwareCampaignItemInfo.cps = cpsCampaignItem;
        firmwareCampaignItemInfo.versionCount = 1L;
        firmwareCampaignItemInfo.createTime = resultSet.getLong(5);
        firmwareCampaignItemInfo.modTime = resultSet.getLong(6);
        firmwareCampaignItemInfo.username = "batch executor";
        firmwareCampaignItemInfo.parent = campaignIds.get(resultSet.getLong(1));
        firmwareCampaignItemInfo.device = resultSet.getLong(2);
        firmwareCampaignItemInfo.deviceMessage = resultSet.getObject(4) == null ? null : resultSet.getLong(4);
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
    }

    private class ServiceCallInfo {
        Long id;
        Long parent;
        Long lastCompletedTime;
        Long state;
        String origin;
        String externalReference;
        String reference;
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
