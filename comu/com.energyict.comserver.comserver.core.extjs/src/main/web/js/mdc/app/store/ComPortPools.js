Ext.define('Mdc.store.ComPortPools',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPortPool'
    ],
    autoLoad: true,
    model: 'Mdc.model.ComPortPool',
    storeId: 'comPortPools',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json',
            root: 'ComPortPools'
        }
    }
});
