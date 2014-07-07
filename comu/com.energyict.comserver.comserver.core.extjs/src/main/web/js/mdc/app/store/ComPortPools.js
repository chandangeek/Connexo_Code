Ext.define('Mdc.store.ComPortPools',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPortPool'
    ],
    model: 'Mdc.model.ComPortPool',
    pageSize: 10,
    storeId: 'comPortPools',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    sortOnLoad: true,
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
