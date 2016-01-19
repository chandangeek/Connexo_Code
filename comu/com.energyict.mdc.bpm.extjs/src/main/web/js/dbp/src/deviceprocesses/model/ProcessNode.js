Ext.define('Dbp.deviceprocesses.model.ProcessNode', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'nodeName',
            type: 'string'
        },
        {
            name: 'nodeType',
            type: 'string'
        },
        {
            name: 'status',
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