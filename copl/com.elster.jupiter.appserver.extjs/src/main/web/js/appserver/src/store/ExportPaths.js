Ext.define('Apr.store.ExportPaths', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ExportPath',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/exportdirs',
        reader: {
            type: 'json',
            root: 'directories'
        }
    }
});

