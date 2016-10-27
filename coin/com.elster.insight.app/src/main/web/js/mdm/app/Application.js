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
        'Dbp.privileges.DeviceProcesses',
        'Fim.privileges.DataImport',
        'Est.privileges.EstimationConfiguration'
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
