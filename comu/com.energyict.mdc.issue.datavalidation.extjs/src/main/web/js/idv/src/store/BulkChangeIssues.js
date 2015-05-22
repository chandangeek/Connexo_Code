Ext.define('Idv.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Idv.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});