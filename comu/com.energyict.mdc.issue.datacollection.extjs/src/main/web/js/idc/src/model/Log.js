Ext.define('Idc.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timestamp', type: 'date', dateFormat: 'time'},
        'details', 'logLevel'
    ]
});

