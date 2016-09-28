Ext.define('Mdc.usagepointmanagement.store.Channels', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.Channel',
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/channels',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'channels'
        }
    }
});