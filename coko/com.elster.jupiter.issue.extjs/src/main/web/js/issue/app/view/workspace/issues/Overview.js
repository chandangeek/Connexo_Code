Ext.define('Mtr.view.workspace.issues.Overview', {
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
                    items: [
                        {
                            html: '<h1>Issues</h1>',
                            margin: '20 10 10 10'
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