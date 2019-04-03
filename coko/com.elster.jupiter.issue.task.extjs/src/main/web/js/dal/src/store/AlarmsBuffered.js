Ext.define('Itk.store.IssuesBuffered', {
    extend: 'Itk.store.Issues',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});
