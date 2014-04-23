Ext.define('Isu.view.workspace.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.workspace-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    content: [
        {
            xtype: 'panel',
            html: '<h1>Workspace</h1>',
            margin: 10
        },
        {
            xtype: 'container',
            layout: 'column',
            margin: 10,
            items: [
                {
                    title: 'Data collection',
                    frame: true,
                    margin: 5,
                    columnWidth: 0.33,
                    bodyPadding: 5,
                    items: [
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/overview">Overview</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/issues">Issues</a>'
                        }
                    ]
                },
                {
                    title: 'Data exchange',
                    frame: true,
                    margin: 5,
                    columnWidth: 0.33,
                    bodyPadding: 5,
                    items: [
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/">Overview</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/issues">Issues</a>'
                        }
                    ]
                },
                {
                    title: 'Data operation',
                    frame: true,
                    margin: 5,
                    columnWidth: 0.33,
                    bodyPadding: 5,
                    items: [
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/">Overview</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/issues">Issues</a>'
                        }
                    ]
                },
                {
                    title: 'Data validation',
                    frame: true,
                    margin: 5,
                    columnWidth: 0.33,
                    bodyPadding: 5,
                    items: [
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/">Overview</a>'
                        },
                        {
                            xtype: 'component',
                            html: '<a href="#/workspace/datacollection/issues">Issues</a>'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
    }
});