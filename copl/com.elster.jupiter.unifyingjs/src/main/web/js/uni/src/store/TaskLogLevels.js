Ext.define('Uni.store.TaskLogLevels', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.TaskLogLevel',
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