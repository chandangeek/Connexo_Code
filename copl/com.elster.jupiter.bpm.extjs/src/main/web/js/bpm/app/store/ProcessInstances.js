Ext.define('Bpm.store.ProcessInstances',{
    extend: 'Ext.data.Store',
    requires: [
        'Bpm.model.ProcessInstance'
    ],
    model: 'Bpm.model.ProcessInstance',
    storeId: 'ProcessInstances',
    remoteSort: true,
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/bpm/runtime/instances',
        reader: {
            type: 'json',
            root: 'instances'
        }
    }
});

