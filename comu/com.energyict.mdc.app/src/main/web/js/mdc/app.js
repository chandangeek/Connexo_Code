Ext.onReady(function () {
    var loader = Ext.create('Uni.Loader'),
        translationsQueue = [
            'CFG',
            'USM',
            'MDC',
            'ISU',
            'DVI',
            'DSH'
        ];

    loader.initI18n(translationsQueue);

    loader.onReady(function () {
        // <debug>
        Ext.Loader.setConfig({
            enabled: true
        });
        // </debug>

        Ext.application({
            name: 'MdcApp',
            extend: 'MdcApp.Application',
            autoCreateViewport: true
        });
    });
});

