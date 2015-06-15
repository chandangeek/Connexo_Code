Ext.define('Dxp.store.DataSelectors', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataSelector',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/selectors',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'selectors'
        }
    }

});
