Ext.define('Uni.store.PendingChanges', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.model.PendingChange'
    ],
    model: 'Uni.model.PendingChange',
    storeId: 'pendingChanges',
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'changes'
        }
    }
});