/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.Application', {
    name: 'Cfg',

    extend: 'Ext.app.Application',

    views: [
    ],

    controllers: [
        'Cfg.controller.Main',
        'Cfg.controller.Validation',
        'Cfg.controller.history.Validation',
        'Cfg.controller.Administration'
    ],

    stores: [
        'ValidationRuleSets'
    ],
    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }

});