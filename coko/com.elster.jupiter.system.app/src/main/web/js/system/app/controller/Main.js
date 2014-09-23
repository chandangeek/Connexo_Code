/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo System Admin',
    defaultToken: '#/administration',
    searchEnabled: false,

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main'
    ]
});
