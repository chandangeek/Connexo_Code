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

