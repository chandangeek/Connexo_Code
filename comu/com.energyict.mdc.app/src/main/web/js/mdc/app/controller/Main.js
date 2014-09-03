/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo Multi Sense',

    packages: [
        {
            name: 'Cfg',
            controller: 'Cfg.controller.Main',
            path: '../../apps/cfg/app'
        },
        {
            name: 'Mdc',
            controller: 'Mdc.controller.Main',
            path: '../../apps/mdc/app'
        }
    ]
});
