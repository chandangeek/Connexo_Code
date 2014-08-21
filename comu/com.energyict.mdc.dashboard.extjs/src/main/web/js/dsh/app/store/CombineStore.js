Ext.define('Dsh.store.CombineStore', {
    extend: 'Ext.data.Store',
    storeId: 'CombineStore',
    model: 'Dsh.model.Combine',
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/breakdown',
        reader: {
            type: 'json',
            root: 'breakdowns'
        }
    }
});

