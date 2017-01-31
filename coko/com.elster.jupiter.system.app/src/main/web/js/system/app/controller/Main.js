/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires:[
        'Usr.privileges.Users',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Sam.privileges.DeploymentInfo',
        'Tme.privileges.Period',
        'Fim.privileges.DataImport',
        'Apr.privileges.AppServer',
        'Cps.privileges.CustomAttributeSets',
        'Mtr.privileges.ReadingTypes',
        'Sct.privileges.ServiceCallType',
        'Bpm.privileges.BpmManagement',
        'Cal.privileges.Calendar',
        'Wss.privileges.Webservices'
    ],

    applicationTitle: 'Connexo Admin',
    defaultToken: '/administration',
    searchEnabled: false,
    onlineHelpEnabled: true,
    privileges:  Ext.Array.merge(
        Usr.privileges.Users.all(),
        Sam.privileges.DataPurge.all(),
        Sam.privileges.License.all(),
        Sam.privileges.DeploymentInfo.all(),
        Tme.privileges.Period.all(),
        Fim.privileges.DataImport.all(),
        Apr.privileges.AppServer.all(),
        Cps.privileges.CustomAttributeSets.all(),
        Mtr.privileges.ReadingTypes.all(),
        Sct.privileges.ServiceCallType.all(),
        Bpm.privileges.BpmManagement.all(),
        Cal.privileges.Calendar.all(),
        Wss.privileges.Webservices.all()
    ),

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Tme.controller.Main',
		'Apr.controller.Main',
        'Fim.controller.Main',
        'Cps.main.controller.Main',
        'Mtr.controller.Main',
        'Sct.controller.Main',
        'Bpm.controller.Main',
        'Cal.controller.Main',
        'Wss.controller.Main'
    ],
    onLaunch: function(){
        this.getController('Bpm.controller.Main').addProcessManagement();
        this.callParent(arguments);
    }
});
