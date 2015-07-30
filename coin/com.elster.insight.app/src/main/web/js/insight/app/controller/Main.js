/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Uni.controller.Navigation',
        'Mtr.controller.Main',
        'InsightApp.controller.insight.Main'
    ],

    applicationTitle: 'Connexo Insight',
    defaultToken: '/insight',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: [],

    controllers: [
        'InsightApp.controller.insight.Main',
        'Mtr.controller.Main',
        'Cfg.controller.Main'
    ]
});
