/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceType;

import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        executeQuery(dataModel,"SELECT * FROM TOU_TU1_CAMPAIGN", this::updateCampaign);
        doTry("Create appKey for service call types", this::updateServiceCallTypes, logger);
    }

    private Boolean updateCampaign(ResultSet resultSet) throws SQLException {
        List<TimeOfUseCampaignInfo> touCampaignInfos = new ArrayList<>();
        while (resultSet.next()) {
            touCampaignInfos.add(update(resultSet));
        }
        if (!touCampaignInfos.isEmpty()) {
            touCampaignInfos.forEach(this::updateCampaignInBd);
        }
        return true;
    }
    private void updateCampaignInBd(TimeOfUseCampaignInfo info) {
        execute(dataModel,"UPDATE TOU_TU1_CAMPAIGN SET CALENDAR_UPLOAD_COMTASK_ID = " +info.calendarUploadComtaskId+ " , VALIDATION_COMTASK_ID = " +info.validationComtaskId+ " WHERE  SERVICECALL = " +info.serviceCall);
    }

    private TimeOfUseCampaignInfo update(ResultSet resultSet) throws SQLException {
        TimeOfUseCampaignInfo touCampaignInfo = new TimeOfUseCampaignInfo();
        touCampaignInfo.serviceCall = resultSet.getLong("SERVICECALL");
        long deviceType = resultSet.getLong("DEVICE_TYPE");
        String activationOption = resultSet.getString("ACTIVATION_OPTION");
        Optional<DeviceType> devType = deviceConfigurationService.findDeviceType(deviceType);
        if(devType.isPresent()){
            devType.get()
                    .getConfigurations().stream()
                    .flatMap( cnf -> cnf.getComTaskEnablements().stream())
                    .forEach(cte ->{
                        if(!cte.isSuspended()) {
                                cte.getComTask().getProtocolTasks().stream()
                                        .filter(protocolTask -> protocolTask instanceof MessagesTask)
                                        .flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream())
                                        .filter(ctg -> ctg.getName().equals("Activity calendar"))
                                        .findAny()
                                        .ifPresent(x -> touCampaignInfo.calendarUploadComtaskId = cte.getComTask().getId());
                            }
                    });

            if(!activationOption.equals("withoutActivation")) {
                touCampaignInfo.validationComtaskId = devType.get().getConfigurations().stream()
                        .flatMap( cnf -> cnf.getComTaskEnablements().stream())
                        .flatMap(cte -> {
                            if(!cte.isSuspended()) {
                                return cte.getComTask().getProtocolTasks().stream();
                            }else{
                                return new ArrayList<ProtocolTask>().stream();
                            }
                        })
                        .filter(protocolTask -> protocolTask instanceof StatusInformationTask)
                        .findAny()
                        .map(cte -> cte.getComTask().getId())
                        .orElse(null);

            }
        }
        return touCampaignInfo;
    }

    private class TimeOfUseCampaignInfo {
        long serviceCall;
        Long calendarUploadComtaskId;
        Long validationComtaskId;
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
