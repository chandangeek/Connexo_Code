Ext.define('Dbp.deviceprocesses.model.ProcessNode', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'type',
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
                return record.get('logDate') ? Uni.DateTime.formatDateTimeLong(new Date(record.get('logDate'))) : '-';
            }
        },
        {
            name: 'nodeInstanceId',
            type: 'number'
        },
        {
            name: 'processInstanceVariables'
        },
        {
            type: 'hasMany',
            model: 'Dbp.deviceprocesses.model.ProcessNodeVariable',
            associationKey: 'processInstanceVariables',
            name: 'processInstanceVariables'
        }

    ]
});