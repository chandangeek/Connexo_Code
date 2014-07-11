Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    itemId: 'issuesOverview',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.Filter',
        'Isu.view.workspace.issues.List',
        'Isu.view.workspace.issues.Item',
        'Isu.view.workspace.issues.SideFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    side: {
        itemId: 'navigation',
        xtype: 'panel',
        ui: 'medium',
        title: 'Navigation',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'overview',
                xtype: 'menu',
                title: 'Overview',
                ui: 'side-menu',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                floating: false,
                plain: true,
                items: [
                    {
                        text: 'Issues',
                        cls: 'current'
                    }
                ]
            },
            {   itemId: 'issues-side-filter',
                xtype: 'issues-side-filter'
            }
        ]
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('workspace.issues.title', 'ISE', 'Issues'),
            items: [
                {   itemId: 'issues-filter',
                    xtype: 'issues-filter'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'issues-list'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('workspace.issues.empty.title', 'ISE', 'No issues found'),
                        reasons: [
                            Uni.I18n.translate('workspace.issues.empty.list.item1', 'ISE', 'No issues have been defined yet.'),
                            Uni.I18n.translate('workspace.issues.empty.list.item2', 'ISE', 'No issues comply to the filter.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'issues-item'
                    }
                }
            ]
        }
    ]
});