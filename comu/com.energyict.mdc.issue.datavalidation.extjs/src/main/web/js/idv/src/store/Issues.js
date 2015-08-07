Ext.define('Idv.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Idv.model.Issue',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/idv/issues',
        reader: {
            type: 'json',
            root: 'dataValidationIssues'
        }
    }
});