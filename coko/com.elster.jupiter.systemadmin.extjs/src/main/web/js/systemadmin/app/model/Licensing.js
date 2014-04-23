Ext.define('Sam.model.Licensing', {
    extend: 'Ext.data.Model',
    alias: 'widget.lic-model',
    fields: [
        {
            name: 'applicationkey',
            type: 'text'
        },
        {
            name: 'id',
            convert: function(value, record) {
                return record.get('applicationkey');
            },
            type: 'text'
        },
        {
            name: 'applicationname',
            type: 'text'
        },
        {
            name: 'status',
            type: 'auto'
        },
        {
            name: 'expires',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'description',
            type: 'auto'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'validfrom',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'graceperiod',
            type: 'auto'
        },
        {
            name: 'content',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/lic/license',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

