Ext.define('Dbp.startprocess.model.AvailableProcess', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'processId',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'displayname',
            type: 'string',
            convert: function (value, record) {
                return record.get('name') + ' (' + record.get('version')+ ')';
            }
        },
        {
            name: 'status',
            type: 'number'
        },
        {
            name: 'version',
            type: 'string'
        }
    ]
});