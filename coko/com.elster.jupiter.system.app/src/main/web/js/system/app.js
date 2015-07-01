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
        },
        {
            name: 'Tme',
            path: '../../apps/tme/src'
        },
        {
            name: 'Yfn',
            path: '../../apps/yfn/src'
        },
		{
            name: 'Apr',
            path: '../../apps/apr/src'
        },
        {
            name: 'Fim',
            path: '../../apps/fim/src'
        }
    ];




    loader.initPackages(packages);
    // </debug>

    loader.onReady(function () {
        Ext.Ajax.defaultHeaders = {
            'X-CONNEXO-APPLICATION-NAME': 'SYS' // a function that return the main application
        };
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

