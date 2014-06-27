Ext.define('Bpm.store.Variables',{
    extend: 'Ext.data.Store',
    requires: [
        'Bpm.model.Variable'
    ],
    model: 'Bpm.model.Variable',
    storeId: 'ProcessVariables',
    remoteSort: true,
    //pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/bpm/runtime/deployment/{deploymentId}/instance/{id}/variables',
        reader: {
            type: 'json',
            root: 'variables'
        }
    }
});