Ext.define('Mdc.usagepointmanagement.model.Channel', {
    extend: 'Ext.data.Model',
    fields: ['dataUntil', 'interval', 'readingType', 'deviceChannels', 'flowUnit'],
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/channels',
        reader: {
            type: 'json'
        }
    }
});