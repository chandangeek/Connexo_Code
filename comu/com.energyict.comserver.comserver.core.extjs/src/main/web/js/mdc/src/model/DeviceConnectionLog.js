Ext.define('Mdc.model.DeviceConnectionLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timeStamp', type: 'date'},
        {name: 'details', type: 'string'},
        {name: 'logLevel', dateFormat: 'time', type: 'date'},
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/connectionmethods/{connectionId}/comsessions/{sessionId}'
    }
});