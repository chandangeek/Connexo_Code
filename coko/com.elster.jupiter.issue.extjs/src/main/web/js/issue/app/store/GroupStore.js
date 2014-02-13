Ext.define('ViewDataCollectionIssues.store.GroupStore', {
    extend: 'Ext.data.Store',
    model: 'ViewDataCollectionIssues.model.GroupModel',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/groupedlist',
        reader: {
            type: 'json',
            root: 'groups',
            totalProperty: 'totalCount'
        }
    }
 });