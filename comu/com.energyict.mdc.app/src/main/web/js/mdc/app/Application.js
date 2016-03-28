Ext.define('MdcApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Sam.privileges.DeploymentInfo',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
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
        'Yfn.privileges.Yellowfin',
        'Isu.privileges.Issue',
        'Dxp.privileges.DataExport',
        'Dlc.privileges.DeviceLifeCycle',
        'Fim.privileges.DataImport',
        'Fwc.privileges.FirmwareCampaign',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses',
        'Mdc.privileges.UsagePoint',
        'Scs.privileges.ServiceCall'
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
