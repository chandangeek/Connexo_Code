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
            path: '../../apps/cfg/src'
        },
        {
            name: 'Mdc',
            controller: 'Mdc.controller.Main',
            path: '../../apps/mdc/src'
        },
        {
            name: 'Isu',
            controller: 'Isu.controller.Main',
            path: '../../apps/issue/src'
        },
        {
            name: 'Dvi',
            controller: 'Dvi.controller.Main',
            path: '../../apps/dvi/src'
        },
        {
            name: 'Dsh',
            controller: 'Dsh.controller.Main',
            path: '../../apps/dashboard/src'
        }
    ]
});
