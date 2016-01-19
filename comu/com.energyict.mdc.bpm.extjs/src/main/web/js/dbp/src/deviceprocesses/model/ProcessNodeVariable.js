Ext.define('Dbp.deviceprocesses.model.ProcessNodeVariable', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'variableName',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        },
        {
            name: 'oldValue',
            type: 'string'
        },
        {
            name: 'logDate',
            type: 'number'
        },
        {
            name: 'logDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('logDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('logDate'))) : '-';
            }
        },
        {
            name: 'nodeInstanceId',
            type: 'number'
        }
    ]
});