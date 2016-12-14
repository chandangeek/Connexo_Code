Ext.define('Mdc.model.ChannelValidationPreview', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'validationActive',
            type: 'boolean'
        },
        {
            name: 'dataValidated',
            type: 'boolean'
        },
        {
            name: 'lastChecked',
            type: 'auto'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/validationpreview',
        reader: {
            type: 'json'
        }
    }
});