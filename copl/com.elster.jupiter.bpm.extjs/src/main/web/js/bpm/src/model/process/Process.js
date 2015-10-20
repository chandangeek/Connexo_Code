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
        },
        {
            name: 'associated',
            type: 'string',
            convert: function (value, record) {
                return 'associated ' + record.get('name');
            }
        },
        {
            name: 'active',
            convert: function (value, record) {
                return record.get('name').indexOf('ss') > 0;
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