Ext.define('Imt.usagepointmanagement.store.BypassStatuses', {
    extend: 'Ext.data.Store',
    fields: ['id', 'displayValue'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/bypassstatus',
        reader: {
            type: 'json',
            root: 'bypassStatus'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});