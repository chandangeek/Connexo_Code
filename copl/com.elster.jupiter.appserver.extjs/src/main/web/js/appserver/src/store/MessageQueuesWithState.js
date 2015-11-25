Ext.define('Apr.store.MessageQueuesWithState', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.MessageQueue',
    autoLoad: false,


    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec?state=true',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'destinationSpecs'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});