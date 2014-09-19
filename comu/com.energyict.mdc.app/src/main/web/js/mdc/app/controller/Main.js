/**
 * @class MdcApp.controller.Main
 */
Ext.define('MdcApp.controller.Main', {
    extend: 'Uni.controller.AppController',

    applicationTitle: 'Connexo MultiSense',
    defaultToken: '#/workspace',

    controllers: [
        'Cfg.controller.Main',
        'Mdc.controller.Main',
        'Isu.controller.Main',
        'Dvi.controller.Main',
        'Dsh.controller.Main'
    ]
});
