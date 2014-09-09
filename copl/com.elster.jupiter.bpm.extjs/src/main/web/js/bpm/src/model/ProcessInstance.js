Ext.define('Bpm.model.ProcessInstance', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'initiator', type: 'string', useNull: true},
        {name: 'version', type: 'string', useNull: true},
        {name: 'state', type: 'int', useNull: true},
        {name: 'startDate', type: 'string', useNull: true},
        {name: 'endDate', type: 'string', useNull: true},
        {name: 'deploymentId', type: 'string', useNull: true}
    ],
    idProperty: 'id',
    proxy: {
        type: 'rest',
        url: '../../api/bpm/runtime/deployment/{deploymentId}/instance',
        reader: {
            type: 'json'
        }
    }
});

