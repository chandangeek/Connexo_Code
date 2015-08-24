Ext.define('Imt.channeldata.model.Channel', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'}
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
