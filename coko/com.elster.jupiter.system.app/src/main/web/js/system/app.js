Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader');

    // <debug>
    // Used only during development to point to hosted files.
    var packages = [
        {
            name: 'Usr',
            path: '../../apps/usr/src'
        },
        {
            name: 'Sam',
            path: '../../apps/sam/src'
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
            name: 'SystemApp',
            extend: 'SystemApp.Application',
            autoCreateViewport: true
        });
    });
});

