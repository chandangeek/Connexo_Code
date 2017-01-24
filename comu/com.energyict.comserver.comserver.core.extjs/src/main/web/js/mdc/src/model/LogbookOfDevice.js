Ext.define('Mdc.model.LogbookOfDevice', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'obisCode', type: 'string'},
        {name: 'overruledObisCode', type: 'string'},
        {name: 'lastEventDate', type: 'long', useNull: true},
        {name: 'lastEventType', type: 'auto'},
        {name: 'lastReading', type: 'long', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/logbooks',
        reader: {
            type: 'json'
        }
    }
});