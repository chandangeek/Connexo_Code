Ext.define('MdmApp.Application', {
    extend: 'Ext.app.Application',

    requires:[
        'Imt.privileges.UsagePoint',
        'Imt.privileges.ServiceCategory',
        'Imt.privileges.MetrologyConfig',
        'Bpm.privileges.BpmManagement',
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
