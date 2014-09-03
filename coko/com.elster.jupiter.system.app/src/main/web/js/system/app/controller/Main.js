/**
 * @class SystemApp.controller.Main
 */
Ext.define('SystemApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo System Admin',

    packages: [
        {
            name: 'Usr',
            controller: 'Usr.controller.Main',
            path: '../../apps/usr/app'
        },
        {
            name: 'Usm',
            controller: 'Usr.controller.Main',
            path: '../../apps/usm/app'
        }
    ]
});
