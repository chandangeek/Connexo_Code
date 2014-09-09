Ext.define('Bpm.store.Nodes',{
    extend: 'Ext.data.Store',
    requires: [
        'Bpm.model.Node'
    ],
    model: 'Bpm.model.Node',
    storeId: 'ProcessNodes',
    remoteSort: true,
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/bpm/runtime/deployment/{deploymentId}/instance/{id}/nodes',
        reader: {
            type: 'json',
            root: 'nodes'
        }
    }
});
