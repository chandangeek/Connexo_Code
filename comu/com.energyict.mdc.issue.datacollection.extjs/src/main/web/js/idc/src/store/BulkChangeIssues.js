Ext.define('Idc.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});