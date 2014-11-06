Ext.define('Dxp.store.FileFormatters', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.FileFormatter',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/processors',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'processors'
        }
    }
});
