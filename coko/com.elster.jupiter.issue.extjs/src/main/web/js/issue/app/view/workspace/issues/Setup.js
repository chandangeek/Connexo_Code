Ext.define('Isu.view.workspace.issues.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-setup',
    itemId: 'issuesOverview',
    issueType: null,
    router: null,

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.SideFilter',
        'Isu.view.workspace.issues.TopPanel',
        'Isu.view.workspace.issues.Grid',
        'Isu.view.workspace.issues.Preview'

        /*'Isu.view.workspace.issues.Filter',
        'Isu.view.workspace.issues.List',
        'Isu.view.workspace.issues.Item',
        'Isu.view.workspace.issues.SideFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'*/
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

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('workspace.issues.title', 'ISE', 'Issues'),
                items: [
                    {   itemId: 'issues-top-panel',
                        xtype: 'issues-top-panel'
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'issues-grid',
                            itemId: 'issues-grid',
                            issueType: me.issueType,
                            router: me.router
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
                            xtype: 'issues-preview',
                            itemId: 'issues-preview',
                            issueType: me.issueType,
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});