Ext.define('InsightApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Imt.privileges.ServiceCategory',
        'Imt.privileges.MetrologyConfig',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses'
    ],

    controllers: [
        'InsightApp.controller.Main'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
        this.callParent(arguments);
    }
});
