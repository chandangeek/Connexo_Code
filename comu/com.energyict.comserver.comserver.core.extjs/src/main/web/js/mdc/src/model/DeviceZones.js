Ext.define('Mdc.model.DeviceZones', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'zoneTypeName', type: 'string'},
        {name: 'zoneTypeId', type: 'int'},
        {name: 'zoneName', type: 'string'},
        {name: 'zoneId', type: 'id'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/zones'
    }
});