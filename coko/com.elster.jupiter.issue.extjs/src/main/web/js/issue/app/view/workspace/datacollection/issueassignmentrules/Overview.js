Ext.define('Isu.view.workspace.datacollection.issueassignmentrules.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Isu.view.workspace.datacollection.issueassignmentrules.Navigation',
        'Isu.view.workspace.datacollection.issueassignmentrules.SideFilter',
        'Isu.view.workspace.datacollection.issueassignmentrules.FilterView',
        'Isu.view.workspace.datacollection.issueassignmentrules.List',
        'Isu.view.workspace.datacollection.issueassignmentrules.Item'
    ],
    alias: 'widget.issue-assignment-rules-overview',
    overflowY: 'auto',

    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        },
        {
            xtype: 'container',
            layout: 'column',
            items: [
                {
                    width: 170,
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'issues-assignment-rules-navigation'
                        },
                        {
                            xtype: 'issues-assignment-rules-side-filter',
                            margin: '15 10 0'
                        }
                    ]
                },
                {
                    columnWidth: 1,
                    items: [
                        {
                            cls: 'content-wrapper',
                            items: [
                                {
                                    html: '<h1>Issue assignment rules</h1>',
                                    margin: '0 0 20 0'
                                },
                                {
                                    xtype: 'issues-assignment-rules-filter-view'
                                },
                                {
                                    html: '<hr>',
                                    margin: '0 0 20 0'
                                },
                                {
                                    xtype: 'issues-assignment-rules-list',
                                    margin: '0 0 20 0'
                                },
                                {
                                    xtype: 'issues-assignment-rules-item'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});