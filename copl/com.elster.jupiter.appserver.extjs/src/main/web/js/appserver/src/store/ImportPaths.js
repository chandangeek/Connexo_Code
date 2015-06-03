Ext.define('Apr.store.ImportPaths', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.ImportPath',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/exportdirs',/*will be import rest*/
        reader: {
            type: 'json',
            root: 'directories'
        }
    }
});

