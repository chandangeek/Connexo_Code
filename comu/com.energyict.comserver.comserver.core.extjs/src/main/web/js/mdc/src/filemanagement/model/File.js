Ext.define('Mdc.filemanagement.model.File', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'creationDate',
            type: 'number'
        },
        {
            name: 'id',
            type: 'number'
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