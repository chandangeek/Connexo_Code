/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpgraderV10_7 implements com.elster.jupiter.upgrade.Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Logger logger;

    @Inject
    public UpgraderV10_7(DataModel dataModel, ServiceCallService serviceCallService,DeviceConfigurationService deviceConfigurationService) {
        this.serviceCallService = serviceCallService;
        this.logger = Logger.getLogger(this.getClass().getName());
        this.dataModel = dataModel;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (
                Connection connection = dataModel.getConnection(true);
                Statement statement = connection.createStatement()
        ){
            executeQuery(statement, "SELECT * FROM TOU_TU1_CAMPAIGN", this::makeCampaignAndSC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        doTry("Create appKey for service call types", this::updateServiceCallTypes, logger);
    }

    private Boolean makeCampaignAndSC(ResultSet resultSet) throws SQLException {
        List<TimeOfUseCampaignInfo> touCampaignInfos = new ArrayList<>();
        while (resultSet.next()) {
            touCampaignInfos.add(makeCampaign(resultSet));
        }
        if (!touCampaignInfos.isEmpty()) {
            touCampaignInfos.forEach(this::writeCampaignInBd);
        }
        return true;
    }
    private void writeCampaignInBd(TimeOfUseCampaignInfo info) {
        execute(dataModel,"UPDATE TOU_TU1_CAMPAIGN SET CALENDAR_UPLOAD_COMTASK_ID = " + info.calendarUploadComtaskId);
        execute(dataModel,"UPDATE TOU_TU1_CAMPAIGN SET VALIDATION_COMTASK_ID = " + info.validationComtaskId);
    }

    private TimeOfUseCampaignInfo makeCampaign(ResultSet resultSet) throws SQLException {
        TimeOfUseCampaignInfo touCampaignInfo = new TimeOfUseCampaignInfo();
        touCampaignInfo.deviceType = resultSet.getLong("DEVICE_TYPE");
        touCampaignInfo.activationOption = resultSet.getString("ACTIVATION_OPTION");
        try {
            deviceConfigurationService.findDeviceType(touCampaignInfo.deviceType)
                    .get().getConfigurations().stream()
                    .flatMap(cnf -> cnf.getComTaskEnablements().stream())
                    .forEach(cte -> cte.getComTask().getProtocolTasks().stream()
                            .filter(protocolTask ->  protocolTask instanceof MessagesTask)
                            .flatMap(task-> ((MessagesTask) task).getDeviceMessageCategories().stream())
                            .filter(ctg -> ctg.getName().equals("Activity calendar"))
                            .findAny()
                            .map(x-> touCampaignInfo.calendarUploadComtaskId = cte.getComTask().getId()).orElse(touCampaignInfo.calendarUploadComtaskId = null));

            if(!touCampaignInfo.activationOption.equals("withoutActivation")) {
                touCampaignInfo.validationComtaskId = deviceConfigurationService.findDeviceType(touCampaignInfo.deviceType)
                        .map(deviceType -> deviceType.getConfigurations().stream()
                                .flatMap( cnf -> cnf.getComTaskEnablements().stream())
                                .flatMap(cte -> cte.getComTask().getProtocolTasks().stream())
                                .filter(protocolTask -> protocolTask instanceof StatusInformationTask)
                                .findAny()
                                .map(cte -> cte.getComTask().getId())
                                .orElse(null))
                        .orElse(null);
            }else{
                touCampaignInfo.validationComtaskId = null;
            }
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return touCampaignInfo;
    }

    private class TimeOfUseCampaignInfo {
        Long serviceCall;
        Long cps;
        Long versionCount;
        Long createTime;
        Long modtime;
        String userName;
        String name;
        String deviceGroup;
        Long activationStart;
        Long activationEnd;
        Long calendar;
        Long deviceType;
        String activationOption;
        Long activationDate;
        String updateType;
        Long validationTimeout;
        String withUniqueCalendarName;
        Long calendarUploadComtaskId;
        Long validationComtaskId;
    }

    private Long toLong(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return null;
        }
    }

    private void updateServiceCallTypes() {
        for (ServiceCallTypes type : ServiceCallTypes.values()) {
            type.getApplication().ifPresent(
                    application ->
                            serviceCallService
                                    .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                                    serviceCallType -> {
                                        serviceCallType.setApplication(application);
                                        serviceCallType.save();
                                    }
                            ));
        }
    }

    void doTry(String description, Runnable runnable, Logger logger) {
        try {
            logger.log(Level.INFO, "Start   : " + description);
            runnable.run();
            logger.log(Level.INFO, "Success : " + description);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        }
    }

}
