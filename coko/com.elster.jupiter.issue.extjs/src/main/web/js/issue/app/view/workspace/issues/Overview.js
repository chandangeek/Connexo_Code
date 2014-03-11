Ext.define('Mtr.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    overflowY: 'auto',
    side: [
        {}
    ],
    content: [
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
});