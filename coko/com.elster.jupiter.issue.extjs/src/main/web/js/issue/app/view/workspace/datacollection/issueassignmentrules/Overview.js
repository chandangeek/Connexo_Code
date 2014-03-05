Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Mtr.view.workspace.datacollection.issueassignmentrules.Navigation',
        'Mtr.view.workspace.datacollection.issueassignmentrules.FilterAction',
        'Mtr.view.workspace.datacollection.issueassignmentrules.FilterView',
        'Mtr.view.workspace.datacollection.issueassignmentrules.List',
        'Mtr.view.workspace.datacollection.issueassignmentrules.Item'
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
                    xtype: 'issues-assignment-rules-filter-action',
                    margin: '15 10 0'
                }
            ]
        },
        {
            xtype: 'container',
            region: 'center',
            items: [
                {
                    defaults: {
                        cls: 'content-wrapper'
                    },
                    items: [
                        {
                            html: '<h1>Issue assignment rules</h1>'
                        },
                        {
                            xtype: 'issues-assignment-rules-filter-view'
                        },
                        {
                            html: '<hr>',
                            cls: '',
                            margin: '0 20'
                        },
                        {
                            xtype: 'issues-assignment-rules-list'
                        },
                        {
                            xtype: 'issues-assignment-rules-item'
                        }
                    ]
                }
            ]

        }
    ]
});