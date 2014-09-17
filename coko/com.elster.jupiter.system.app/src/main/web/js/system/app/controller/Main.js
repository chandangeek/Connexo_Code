/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo System Admin',
    defaultToken: '#/administration',

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main',
        'Bpm.controller.Main'
    ]
});
