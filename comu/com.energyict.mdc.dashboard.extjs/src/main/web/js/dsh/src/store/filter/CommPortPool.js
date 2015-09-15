Ext.define('Dsh.store.filter.CommPortPool', {
    extend: 'Ext.data.Store',
    fields: ['name', 'id'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comportpools',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'comPortPools'
        }
    }
});
