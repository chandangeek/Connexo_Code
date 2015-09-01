Ext.define('Imt.channeldata.model.Channel', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'deviceName', type: 'string'},
        {name: 'readingType', type: 'auto'},
        {name: 'readingTypemRID', type: 'string', mapping: 'readingType.mRID', persist: false},
        {name: 'readingTypeFullAliasName', type: 'string', mapping: 'readingType.fullAliasName', persist: false},
        {name: 'interval', type: 'auto'},
        {name: 'lastValueTimestamp', type: 'auto'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/',
        timeout: 240000,
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID));
        }
    }
});
