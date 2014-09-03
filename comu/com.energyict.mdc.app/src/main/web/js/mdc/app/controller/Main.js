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
        },
        {
            name: 'Ext.ux.Rixo',
            path: '../../apps/issue/resources/js/Ext/ux/Rixo'
        },
        {
            name: 'Skyline',
            path: '../../apps/ext/packages/uni-theme-skyline/src'
        },
        {
            name: 'Isu',
            controller: 'Isu.controller.Main',
            path: '../../apps/issue/app'
        },
        {
            name: 'Dvi',
            controller: 'Dvi.controller.Main',
            path: '../../apps/dvi/app'
        },
        {
            name: 'Dsh',
            controller: 'Dsh.controller.Main',
            path: '../../apps/dashboard/app'
        }
    ]
});
