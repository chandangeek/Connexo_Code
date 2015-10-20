Ext.define('Bpm.model.process.BpmProcess', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string',
            convert: function (value, record) {
                return 'deployment ' + record.get('name');
            }
        },
        {
            name: 'version',
            type: 'string',
            convert: function (value, record) {
                return 'version ' + record.get('name');
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/processes',
        reader: {
            type: 'json'
        }
    }
});