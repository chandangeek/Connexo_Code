Ext.define('Isu.store.IssueComments', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueComments',
    autoLoad: false,

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});