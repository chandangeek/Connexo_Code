Ext.define('Mdc.model.DeviceConnectionLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timestamp', dateFormat: 'time', type: 'date'},
        {name: 'details', type: 'string'},
        {name: 'errorDetails', type: 'string'},
        {name: 'logLevel', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions/{sessionId}/journals'
    }
});