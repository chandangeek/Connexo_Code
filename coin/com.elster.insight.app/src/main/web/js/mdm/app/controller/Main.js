/**
 * @class MdmApp.controller.Main
 */
Ext.define('MdmApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Cfg.controller.Main',
        'Uni.controller.Navigation',
        'Imt.controller.Main',
        'Imt.privileges.UsagePoint',
        'Imt.privileges.ServiceCategory'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/search',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: Ext.Array.merge(
        Imt.privileges.ServiceCategory.all(),
        Imt.privileges.UsagePoint.all()
    ),

    controllers: [
        'Cfg.controller.Main',
        'Imt.controller.Main'
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
