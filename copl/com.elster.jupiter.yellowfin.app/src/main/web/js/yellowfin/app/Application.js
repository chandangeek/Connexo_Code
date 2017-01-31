/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('YellowfinApp.Application', {
    extend: 'Ext.app.Application',

    controllers: [
        'YellowfinApp.controller.Main'
    ],

    launch: function () {
        // Removes the loading indicator.
        Ext.fly('appLoadingWrapper').destroy();

        this.callParent(arguments);
    }
});
