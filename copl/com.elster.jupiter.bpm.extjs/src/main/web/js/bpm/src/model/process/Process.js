Ext.define('Bpm.model.process.Process', {
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
            name: 'associated',
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