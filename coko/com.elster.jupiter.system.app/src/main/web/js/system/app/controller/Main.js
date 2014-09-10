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
            path: '../../apps/usr/src'
        },
        {
            name: 'Sam',
            controller: 'Sam.controller.Main',
            path: '../../apps/sam/src'
        }
    ]
});
