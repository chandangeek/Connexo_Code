/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Imt.privileges.UsagePointLifeCycle',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses',
        'Fim.privileges.DataImport',
        'Dxp.privileges.DataExport',
        'Est.privileges.EstimationConfiguration',
        'Imt.privileges.UsagePointGroup',
        'Yfn.privileges.Yellowfin'
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
