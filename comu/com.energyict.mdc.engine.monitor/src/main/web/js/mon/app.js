/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.onReady(function () {
    Ext.Loader.setConfig({
        enabled: true,
        disableCaching: true // For debug only.
    });

    // Start up the application.
    Ext.application({

        name: 'CSMonitor',

        extend: 'CSMonitor.Application',

        autoCreateViewport: true
    });
});

