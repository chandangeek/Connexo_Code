/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Cfg.controller.Main',
        'Uni.controller.Navigation',
        'Imt.controller.Main',
        'Bpm.controller.Main',
        'Dbp.controller.Main',
        'Imt.privileges.ServiceCategory',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/search',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: Ext.Array.merge(
        Imt.privileges.ServiceCategory.all(),
        Bpm.privileges.BpmManagement.all(),
        Dbp.privileges.DeviceProcesses.all()
    ),

    controllers: [
        'Cfg.controller.Main',
        'Imt.controller.Main',
        'Bpm.controller.Main',
        'Dbp.controller.Main'
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
