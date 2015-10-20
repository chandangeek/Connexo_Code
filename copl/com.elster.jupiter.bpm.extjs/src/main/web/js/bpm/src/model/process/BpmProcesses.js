Ext.define('Bpm.model.process.BpmProcesses', {
    extend: 'Ext.data.Model',
    requires: 'Bpm.model.process.BpmProcess',
    fields: [
        'processes'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Bpm.model.process.BpmProcess',
            associationKey: 'processes',
            name: 'processes'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/processes',
        reader: {
            type: 'json',
            root: 'processes'
        }
    }
});