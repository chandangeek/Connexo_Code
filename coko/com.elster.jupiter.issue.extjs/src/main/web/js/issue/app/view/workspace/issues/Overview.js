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