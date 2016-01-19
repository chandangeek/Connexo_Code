Ext.define('Dbp.deviceprocesses.model.ProcessNodes', {
    extend: 'Ext.data.Model',
    requires: [
        'Dbp.deviceprocesses.model.ProcessNodeVariable',
        'Dbp.deviceprocesses.model.ProcessNode'
    ],
    fields: [
        {
            name: 'processInstanceStatus'
        },
        {
            name: 'processInstanceNodes'
        },
        {
            name: 'processInstanceVariables'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Dbp.deviceprocesses.model.ProcessNode',
            associationKey: 'processInstanceNodes',
            name: 'processInstanceNodes'
        },
        {
            type: 'hasMany',
            model: 'Dbp.deviceprocesses.model.ProcessNodeVariable',
            associationKey: 'processInstanceVariables',
            name: 'processInstanceVariables'
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/bpm/runtime/process/instance/{processId}/nodes',
        reader: {
            type: 'json'
        },
        setUrl: function (processId) {
            this.url = this.urlTpl.replace('{processId}', encodeURIComponent(processId));
        }
    }
});