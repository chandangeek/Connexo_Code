Ext.define('Dsh.store.filter.CommPortPool', {
    extend: 'Ext.data.Store',
    fields: ['name', 'id'],
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comportpools',
        reader: {
            type: 'json',
            root: 'comPortPools'
        }
    }
});
