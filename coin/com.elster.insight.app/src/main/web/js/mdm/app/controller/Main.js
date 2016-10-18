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
        'Bpm.controller.Main',
        'Dbp.controller.Main',
        'Bpm.privileges.BpmManagement',
        'Dbp.privileges.DeviceProcesses',
        'Fim.controller.Main',
        'Fim.privileges.DataImport'
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
        Bpm.privileges.BpmManagement.all(),
        Dbp.privileges.DeviceProcesses.all(),
        Fim.privileges.DataImport.all()
    ),

    controllers: [
        'Sam.controller.Main',
        'Scs.controller.Main',
        'Cfg.controller.Main',
        'Imt.controller.Main',
        'Bpm.controller.Main',
        'Dbp.controller.Main',
        'Fim.controller.Main'
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
