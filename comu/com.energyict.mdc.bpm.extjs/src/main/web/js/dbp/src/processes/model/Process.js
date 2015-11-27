Ext.define('Dbp.processes.model.Process', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'associatedTo',
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process/activate',
        reader: {
            type: 'json'
        }
    }
});