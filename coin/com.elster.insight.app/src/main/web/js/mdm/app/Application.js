Ext.define('MdmApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Sam.privileges.DeploymentInfo',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Scs.privileges.ServiceCall',
        'Imt.privileges.UsagePoint',
        'Imt.privileges.ServiceCategory',
        'Imt.privileges.MetrologyConfig',
        'Bpm.privileges.BpmManagement',
        'Dxp.privileges.DataExport',
        'Dbp.privileges.DeviceProcesses'
    ],

    controllers: [
        'MdmApp.controller.Main'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
