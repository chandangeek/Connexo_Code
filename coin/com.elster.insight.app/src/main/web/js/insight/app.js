//Ext.require('Uni.Loader');

Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    // Used only during development to point to hosted files.
    var packages = [
        {
            name: 'Cfg',
            path: '../../apps/cfg/src'
        },
        {
            name: 'Est',
            path: '../../apps/est/src'
        }
    ];

    loader.initPackages(packages);
    // </debug>

    loader.onReady(function () {
        // <debug>
        Ext.Loader.setConfig({
            enabled: true
        });
        // </debug>

        Ext.application({
            name: 'InsightApp',
            extend: 'InsightApp.Application',
            autoCreateViewport: true
        });
    });
});

