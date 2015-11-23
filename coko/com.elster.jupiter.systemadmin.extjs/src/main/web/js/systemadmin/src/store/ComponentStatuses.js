Ext.define('Sam.store.ComponentStatuses', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.ApplicationStatus',

    proxy: {
        type: 'rest',
        url: '/api/sys/fields/componentStatuses',
        reader: {
            type: 'json',
            root: 'componentStatuses'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});