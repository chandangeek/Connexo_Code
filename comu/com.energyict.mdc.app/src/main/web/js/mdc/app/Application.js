Ext.define('MdcApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Cfg.privileges.Validation',
        'Mdc.privileges.DeviceGroup',
        'Mdc.privileges.DeviceType',
        'Mdc.privileges.MasterData',
        'Mdc.privileges.Communication',
        'Mdc.privileges.CommunicationSchedule'
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
