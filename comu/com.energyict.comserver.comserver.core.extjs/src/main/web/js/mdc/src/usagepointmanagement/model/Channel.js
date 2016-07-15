Ext.define('Mdc.usagepointmanagement.model.Channel', {
    extend: 'Ext.data.Model',
    fields: ['dataUntil', 'interval', 'readingType', 'deviceChannels', 'flowUnit'],
    proxy: {
        type: 'rest',
        urlTpl: '/api/upr/usagepoints/{mRID}/channels',
        reader: {
            type: 'json'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});