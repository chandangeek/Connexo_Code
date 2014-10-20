Ext.define('Mdc.model.DeviceConnectionHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'connectionMethod', type: 'string'},
        {name: 'startedOn', dateFormat: 'time', type: 'date'},
        {name: 'finishedOn', dateFormat: 'time', type: 'date'},
        {name: 'durationInSeconds', type: 'int'},
        {name: 'direction', type: 'string'},
        {name: 'connectionType', type: 'string'},
        'comServer',
        {name: 'comPort', type: 'string'},
        {name: 'status', type: 'string'},
        'result',
        'comTaskCount',
        {name: 'isDefault', type: 'boolean'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mRID}/connectionmethods/{connectionId}/comsessions'
    }
});