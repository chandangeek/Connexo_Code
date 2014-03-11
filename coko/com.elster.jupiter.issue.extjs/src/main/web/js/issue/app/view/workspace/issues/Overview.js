Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Ext.container.Container',
    requires: [
        'Uni.view.breadcrumb.Trail'
    ],
    alias: 'widget.issues-overview',
    overflowY: 'auto',

    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        },
        {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    defaults: {
                        cls: 'content-wrapper'
                    },
                    items: [
                        {
                            border: false,
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    html: '<h1>Issues</h1>'
                                },
                                {
                                    flex: 1,
                                    layout: {
                                        type: 'hbox',
                                        align: 'middle'
                                    },
                                    items: [
                                        {
                                            flex: 1
                                        },
                                        {
                                            html: '<a href="#/workspace/datacollection/assignmentrules">View assignment rules</a>',
                                            width: 150
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'issues-filter'
                        },
                        {
                            xtype: 'issue-no-group'
                        },
                        {
                            xtype: 'issues-list'
                        },
                        {
                            xtype: 'issues-item'
                        }
                    ]
                }
            ]
        }
    ]
});