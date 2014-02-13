Ext.define('ViewDataCollectionIssues.view.ViewDataCollectionIssuesBlank', {
    extend: 'Ext.panel.Panel',
    xtype: 'view-data-collection-issues-blank',
    store: 'ViewDataCollectionIssues.store.DataCollectionIssuesList',
    hidden: true,
    html: '<h2>No issue found</h2><p>No data collection issues have been created yet.</p>'
});