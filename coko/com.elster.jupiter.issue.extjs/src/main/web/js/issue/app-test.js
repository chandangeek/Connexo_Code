Ext.require('Uni.Loader');

Ext.Loader.setConfig({
    enabled: true,
    disableCaching: true, // For debug only.
    paths: {
        'Uni' : '../uni/src'
    }
});

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');
    loader.initI18n(['MTR']);

    loader.onReady(function () {
        // Start up the application.
        Ext.application({
            name: 'Mtr',

            extend: 'Mtr.Application',

            autoCreateViewport: true,

            launch: function () {
                jasmine.getEnv().addReporter(new jasmine.TrivialReporter());
                jasmine.getEnv().execute();
            }
        });
    });
});