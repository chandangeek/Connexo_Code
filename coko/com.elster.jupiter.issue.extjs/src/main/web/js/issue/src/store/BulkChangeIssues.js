Ext.define('Isu.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});