/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.Application', {
    name: 'Bpm',

    extend: 'Ext.app.Application',

    requires: [
        'Bpm.controller.Main'
    ],

    views: [
        // Views are loaded in through their respective controller.
    ],

    controllers: [
        'Bpm.controller.Main'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }
});