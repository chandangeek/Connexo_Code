/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo System Admin',
    defaultToken: '#/administration',
    searchEnabled: false,
    privileges: ['privilege.administrate.userAndRole','privilege.view.userAndRole','privilege.view.license','privilege.upload.license'],

    controllers: [
        'Usr.controller.Main',
        'Sam.controller.Main'
    ]
});
