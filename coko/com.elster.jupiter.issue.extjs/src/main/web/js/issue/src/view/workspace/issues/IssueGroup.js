Ext.define('Isu.view.workspace.issues.IssueGroup', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.workspace.issues.GroupGrid'
    ],
    itemId: 'IssueGroup',
    alias: 'widget.issue-group',
    hidden: true,
    items: [
        {
            itemId: 'issue-group-grid',
            xtype: 'issue-group-grid'
        },
        {
            name: 'issue-group-info',
            hidden: true
        }
    ]
});