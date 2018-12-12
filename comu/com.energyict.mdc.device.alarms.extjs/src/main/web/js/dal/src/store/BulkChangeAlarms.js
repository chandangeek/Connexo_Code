Ext.define('Dal.store.BulkChangeAlarms', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.BulkChangeAlarms',

    requires: [
        'Ext.data.proxy.SessionStorage'
    ],

    proxy: {
        type: 'sessionstorage',
        id  : 'bulkProxy'
    }
});
