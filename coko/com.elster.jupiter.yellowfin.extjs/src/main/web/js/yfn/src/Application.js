/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.Application', {
    name: 'Yfn',
    extend: 'Ext.app.Application',
    requires: [
        'Yfn.privileges.Yellowfin',
        'Yfn.controller.Main'
    ],

    views: [
    ],

    controllers: [
        'Yfn.controller.Main'
    ],

    stores: [
        // Stores are required through their controllers.
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();
    }
});