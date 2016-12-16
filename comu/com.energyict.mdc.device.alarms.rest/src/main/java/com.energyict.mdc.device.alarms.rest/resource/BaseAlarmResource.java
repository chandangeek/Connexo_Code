package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;

import javax.inject.Inject;


public class BaseAlarmResource {

    private DeviceAlarmService deviceAlarmService;
    private IssueService issueService;
    private MeteringService meteringService;
    private UserService userService;
    private Thesaurus thesaurus;

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
}
