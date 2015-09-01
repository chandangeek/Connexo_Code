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
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelID}/validationpreview',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelID}', channelId);
        }
    }
});