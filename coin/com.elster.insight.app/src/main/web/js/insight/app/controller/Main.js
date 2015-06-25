/**
 * @class InsightApp.controller.Main
 */
Ext.define('InsightApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    requires: [
        'Uni.controller.Navigation',
        'InsightApp.controller.insight.Main'
    ],

    applicationTitle: 'Connexo Insight',
    defaultToken: '/insight',
    searchEnabled: true,
    onlineHelpEnabled: false,

    privileges: [],

    controllers: [
        'InsightApp.controller.insight.Main',
        'Cfg.controller.Main',
        'Est.main.controller.Main'
    ]
});
