Ext.define('Idc.store.IssuesBuffered', {
    requires:[
        'Isu.store.Issues'
    ],
    extend: 'Isu.store.Issues',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});