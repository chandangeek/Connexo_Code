/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class MdmApp.controller.Main
 */
Ext.define('MdmApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Sam.privileges.DeploymentInfo',
        'Sam.privileges.DataPurge',
        'Sam.privileges.License',
        'Cfg.controller.Main',
        'Uni.controller.Navigation',
        'Scs.privileges.ServiceCall',
        'Imt.controller.Main',
        'Imt.privileges.UsagePoint',
        'Imt.privileges.ServiceCategory',
        'Imt.privileges.UsagePointLifeCycle',
        'Bpm.controller.Main',
        'Dbp.controller.Main',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses',
        'Fim.controller.Main',
        'Fim.privileges.DataImport',
        'Dxp.privileges.DataExport',
        'Est.main.controller.Main',
        'Est.privileges.EstimationConfiguration',
        'Imt.privileges.UsagePointGroup',
        'Yfn.privileges.Yellowfin',
        'Yfn.controller.Main'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/search',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: Ext.Array.merge(
        Sam.privileges.DeploymentInfo.all(),
        Sam.privileges.DataPurge.all(),
        Sam.privileges.License.all(),
        Scs.privileges.ServiceCall.all(),
        Imt.privileges.ServiceCategory.all(),
        Imt.privileges.UsagePoint.all(),
        Imt.privileges.UsagePointGroup.all(),
        Imt.privileges.UsagePointLifeCycle.all(),
        Bpm.privileges.BpmManagement.all(),
        Dbp.privileges.DeviceProcesses.all(),
        Fim.privileges.DataImport.all(),
        Est.privileges.EstimationConfiguration.all(),
        Dxp.privileges.DataExport.all(),
        Yfn.privileges.Yellowfin.all()
    ),

    controllers: [
        'Sam.controller.Main',
        'Scs.controller.Main',
        'Cfg.controller.Main',
        'Imt.controller.Main',
        'Bpm.controller.Main',
        'Dbp.controller.Main',
        'Fim.controller.Main',
        'Est.main.controller.Main',
        'Dxp.controller.Main',
        'Yfn.controller.Main'
    ],
    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        
        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(1, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);
    }
});
