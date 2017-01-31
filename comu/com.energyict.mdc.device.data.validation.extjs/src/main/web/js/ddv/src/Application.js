/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.Application', {
    name: 'Ddv',

    extend: 'Ext.app.Application',

    requires: [
        'Ddv.controller.Main'
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Ddv.controller.Main',
        'Ddv.controller.Validations'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }
});