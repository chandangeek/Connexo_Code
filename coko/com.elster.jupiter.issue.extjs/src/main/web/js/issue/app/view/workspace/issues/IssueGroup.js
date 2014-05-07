Ext.define('Isu.view.workspace.issues.IssueGroup', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.workspace.issues.GroupGrid'
    ],
    alias: 'widget.issue-group',
    hidden: true,
    items: [
        {xtype: 'issue-group-grid'},
        {
            alias: 'widget.issue-group-info'
        }
    ]
});