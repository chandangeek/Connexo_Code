Ext.define('Mdc.model.DeviceZone', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'zoneId', type: 'int'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/zones'
    }
});