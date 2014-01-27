Ext.define('Mdc.store.ComPortPools',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPortPool'
    ],
    model: 'Mdc.model.ComPortPool',
    storeId: 'comPortPools',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json',
            root: 'comPortPools'
        }
    }
});
