/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Uni.controller.Navigation',
        'Mtr.controller.Main'
    ],

    applicationTitle: 'Connexo Insight',
    applicationKey: 'INS',
    defaultToken: '/insight',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: [],

    controllers: [
        'Cfg.controller.Main',
        'Mtr.controller.Main'
    ],
    init: function () {
        var router = this.getController('Uni.controller.history.Router');

        // default route redirect
        router.initRoute('default', {
            redirect: this.defaultToken.slice(2, this.defaultToken.length),
            route: ''
        });

        this.callParent(arguments);
    }
});
