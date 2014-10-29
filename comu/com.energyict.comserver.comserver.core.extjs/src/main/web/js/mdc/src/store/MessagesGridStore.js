Ext.define('Mdc.store.MessagesGridStore', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Message'
    ],
    model: 'Mdc.model.Message',
    storeId: 'MessagesGridStore'
});
