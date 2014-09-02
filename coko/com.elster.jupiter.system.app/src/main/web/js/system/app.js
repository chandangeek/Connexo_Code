Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader'),
        translationsQueue = [
            'CFG',
            'USR'
        ];

    loader.initI18n(translationsQueue);

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

