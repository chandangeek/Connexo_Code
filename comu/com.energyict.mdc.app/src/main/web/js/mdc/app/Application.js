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
        'Mdc.privileges.DeviceConfigurationEstimations',
        'Dvi.privileges.InventoryManagement',
        'Yfn.privileges.Yellowfin',
        'Isu.privileges.Issue',
        'Dxp.privileges.DataExport',
        'Dlc.privileges.DeviceLifeCycle'
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
