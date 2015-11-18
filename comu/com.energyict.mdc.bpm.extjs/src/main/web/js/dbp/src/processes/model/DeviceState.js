Ext.define('Dbp.processes.model.DeviceState', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'deviceState',
            type: 'string'
        },
        {
            name: 'deviceStateId',
            type: 'number'
        }
    ]
});