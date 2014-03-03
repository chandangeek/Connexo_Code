Ext.define('Mtr.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Mtr.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});