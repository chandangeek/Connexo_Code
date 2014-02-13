Ext.define('ViewDataCollectionIssues.store.DataCollectionIssuesList', {
    extend: 'Ext.data.Store',
    requires:[
        'Ext.data.proxy.Rest'
    ],
    model: 'ViewDataCollectionIssues.model.DataCollectionIssue',
    pageSize: 10,
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json',
            root: 'issueList'
        }
    }
 });