Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Issue',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/idc/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
