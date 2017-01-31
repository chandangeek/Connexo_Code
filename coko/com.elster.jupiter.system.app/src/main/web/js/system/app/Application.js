/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('SystemApp.Application', {
    extend: 'Ext.app.Application',
    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Sam.privileges.DeploymentInfo',
        'Apr.privileges.AppServer',
        'Yfn.privileges.Yellowfin',
        'Tme.privileges.Period',
        'Fim.privileges.DataImport',
        'Cps.privileges.CustomAttributeSets',
        'Mtr.privileges.ReadingTypes',
        'Sct.privileges.ServiceCallType',
        'Bpm.privileges.BpmManagement',
        'Cal.privileges.Calendar',
        'Wss.privileges.Webservices'
    ],

    controllers: [
        'SystemApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();

        this.callParent(arguments);
    }
});
