Ext.define('Idc.store.IssuesBuffered', {
    extend: 'Idc.store.Issues',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});