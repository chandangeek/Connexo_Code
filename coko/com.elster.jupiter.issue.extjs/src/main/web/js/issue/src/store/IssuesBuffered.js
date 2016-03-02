Ext.define('Isu.store.IssuesBuffered', {
    extend: 'Isu.store.Issues',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});
