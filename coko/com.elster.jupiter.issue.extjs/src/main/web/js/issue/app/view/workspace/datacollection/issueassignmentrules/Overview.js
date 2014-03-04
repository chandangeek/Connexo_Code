Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Mtr.view.workspace.datacollection.issueassignmentrules.Navigation',
        'Mtr.view.workspace.datacollection.issueassignmentrules.Filter',
        'Mtr.view.workspace.datacollection.issueassignmentrules.List'
    ],
    alias: 'widget.issue-assignment-rules-overview',
    layout: 'border',
    overflowY: 'auto',

    items: [
        {
            xtype: 'breadcrumbTrail',
            region: 'north',
            padding: 6
        },
        {
            xtype: 'container',
            region: 'west',
            width: 170,
            items: [
                {
                    xtype: 'issues-assignment-rules-navigation'
                },
                {
                    xtype: 'issues-assignment-rules-filter'
                }
            ]
        },
        {
            xtype: 'container',
            region: 'center',
            cls: 'content-wrapper',
            style: {
                backgroundColor: '#fff'
            },
            items: [
                {
                    html: '<h1>Issue assignment rules</h1>'
                },
                {
                    xtype: 'issues-assignment-rules-list'
                }
            ]
        }
    ]
});