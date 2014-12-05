Ext.define('Sam.model.DataPurgeLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timestamp', dateFormat: 'time', type: 'date'},
        'logLevel',
        'message'
    ]
});