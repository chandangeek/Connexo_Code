Ext.define('Mdc.model.DeviceZones', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'zoneTypeName', type: 'string'},
        {name: 'zoneTypeId', type: 'int', useNull: true},
        {name: 'zoneName', type: 'string'},
        {name: 'zoneId', type: 'int', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/zones'
    }
});