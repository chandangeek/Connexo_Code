package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

public class FirmwareCampaignExecutor implements TaskExecutor {

    private final FirmwareServiceImpl firmwareService;

    public FirmwareCampaignExecutor(FirmwareServiceImpl firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        cloneDevicesForFirmwareCampaigns();
        launchFirmwareCampaigns();
        updateDeviceStatuses();
    }

    private void cloneDevicesForFirmwareCampaigns(){
        firmwareService.getFirmwareCampaignsForDeviceCloning().stream().forEach(firmwareCampaign -> firmwareCampaign.cloneDeviceList());
    }

    private void launchFirmwareCampaigns(){
        firmwareService.getFirmwareCampaignsForProcessing().stream().forEach(firmwareCampaign -> firmwareCampaign.start());
    }

    private void updateDeviceStatuses(){

    }
}
