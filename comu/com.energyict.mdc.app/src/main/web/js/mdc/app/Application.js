/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('MdcApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Cfg.privileges.Validation',
        'Mdc.privileges.MasterData',
        'Mdc.privileges.DeviceGroup',
        'Mdc.privileges.DeviceType',
        'Mdc.privileges.Device',
        'Mdc.privileges.CommunicationSchedule',
        'Mdc.privileges.DeviceSecurity',
        'Mdc.privileges.DeviceCommands',
        'Mdc.privileges.Communication',
        'Mdc.privileges.DataCollectionKpi',
        'Mdc.privileges.DeviceConfigurationEstimations',
        'Mdc.privileges.Monitor',
        'Yfn.privileges.Yellowfin',
        'Isu.privileges.Issue',
        'Dxp.privileges.DataExport',
        'Dlc.privileges.DeviceLifeCycle',
        'Fim.privileges.DataImport',
        'Fwc.privileges.FirmwareCampaign',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses',
        'Mdc.privileges.UsagePoint',
        'Scs.privileges.ServiceCall',
        'Mdc.privileges.Monitor',
        'Mdc.privileges.MetrologyConfiguration',
        'Mdc.privileges.CommandLimitationRules',
        'Dal.privileges.Alarm',
        'Mdc.privileges.RegisteredDevicesKpi',
        'Mdc.privileges.TaskManagement',
        'Mdc.privileges.SecurityAccessor',
        'Cfg.privileges.Audit',
        'Tou.privileges.TouCampaign',
        'Itk.privileges.Task',
        'Mdc.privileges.CreationRule'
    ],
    controllers: [
        'MdcApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
