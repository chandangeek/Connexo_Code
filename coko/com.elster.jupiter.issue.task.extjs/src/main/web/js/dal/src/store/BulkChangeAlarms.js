Ext.define('Itk.store.BulkChangeIssues', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.BulkChangeIssues',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});
