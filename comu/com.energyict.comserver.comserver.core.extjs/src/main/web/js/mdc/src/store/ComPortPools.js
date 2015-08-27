Ext.define('Mdc.store.ComPortPools',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPortPool'
    ],
    model: 'Mdc.model.ComPortPool',
    storeId: 'comPortPools',
/*    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    sortOnLoad: true,*/
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
