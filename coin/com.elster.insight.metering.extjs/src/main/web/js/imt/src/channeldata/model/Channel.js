Ext.define('Imt.channeldata.model.Channel', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'deviceName', type: 'string'},
        {name: 'readingTypemRID', type: 'string'},
        {name: 'readingTypeAlias', type: 'string'},
        {name: 'interval', type: 'auto'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/channels',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'channelInfos'
        }
    }
});
