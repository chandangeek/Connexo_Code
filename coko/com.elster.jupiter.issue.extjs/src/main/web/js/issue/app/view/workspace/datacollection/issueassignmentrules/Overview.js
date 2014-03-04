Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Mtr.view.workspace.datacollection.issueassignmentrules.List'
    ],
    alias: 'widget.issue-assignment-rules-overview',
    overflowY: 'auto',

    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        },
        {
            xtype: 'issues-assignment-rules-list'
        }
    ]
});