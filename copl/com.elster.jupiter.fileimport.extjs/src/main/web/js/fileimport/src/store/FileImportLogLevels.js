Ext.define('Fim.store.FileImportLogLevels', {
    extend: 'Ext.data.Store',
    model: 'Fim.model.FileImportLogLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/rut/loglevels',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'logLevels'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
