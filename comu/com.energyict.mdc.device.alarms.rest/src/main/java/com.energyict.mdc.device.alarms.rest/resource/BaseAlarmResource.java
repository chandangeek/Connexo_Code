/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;

import javax.inject.Inject;


public class BaseAlarmResource {

    private DeviceAlarmService deviceAlarmService;
    private IssueService issueService;
    private MeteringService meteringService;
    private UserService userService;
    private Thesaurus thesaurus;
    private PropertyValueInfoService propertyValueInfoService;
    private IssueActionService issueActionService;
    private TransactionService transactionService;

    public BaseAlarmResource(){

    }

    @Inject
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Inject
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    protected DeviceAlarmService getDeviceAlarmService() {
        return deviceAlarmService;
    }

    @Inject
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    protected UserService getUserService() {
        return userService;
    }

    public PropertyValueInfoService getPropertyValueInfoService() {
        return propertyValueInfoService;
    }

    @Inject
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public IssueActionService getIssueActionService() {
        return issueActionService;
    }

    @Inject
    public void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
